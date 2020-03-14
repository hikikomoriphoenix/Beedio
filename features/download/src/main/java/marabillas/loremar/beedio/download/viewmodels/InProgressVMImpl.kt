/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.download.viewmodels

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import marabillas.loremar.beedio.base.database.DownloadItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.DetailsFetchWorker
import marabillas.loremar.beedio.base.download.DownloadFileValidator
import marabillas.loremar.beedio.base.download.DownloadQueueManager
import marabillas.loremar.beedio.base.download.DownloadQueueManager.Companion.DETAILS_FETCH_WORKER
import marabillas.loremar.beedio.base.download.DownloadQueueManager.Companion.NEXT_DOWNLOAD_WORKER
import marabillas.loremar.beedio.base.download.DownloadQueueManager.Companion.VIDEO_DETAILS_FILE
import marabillas.loremar.beedio.base.download.DownloadQueueManager.Companion.VIDEO_DOWNLOAD_WORKER
import marabillas.loremar.beedio.base.download.VideoDownloader
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.media.VideoDetailsTypeAdapter
import marabillas.loremar.beedio.base.mvvm.SendLiveData
import java.io.File
import java.io.FileReader
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class InProgressVMImpl(private val context: Context, downloadDB: DownloadListDatabase) : InProgressVM() {
    private val inProgressDao = downloadDB.downloadListDao()

    private val listOperationDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val progressTrackingDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private var progressTrackingJob: Job? = null

    private var queueEventObserver: QueueEventObserver? = null
    private val downloadStateObserver = DownloadStateObserver()
    private val gson = GsonBuilder()
            .registerTypeAdapter(VideoDetails::class.java, VideoDetailsTypeAdapter())
            .create()
    private val downloadFileValidator = DownloadFileValidator(context)

    private val progressUpdate = SendLiveData<ProgressUpdate>()
    private val inProgressListUpdate = SendLiveData<List<InProgressItem>>()
    private val _isDownloading = MutableLiveData<Boolean>()
    private val _isFetching = MutableLiveData<Boolean>()
    private val videoDetails = SendLiveData<VideoDetails>()

    override val isDownloading: Boolean?
        get() = _isDownloading.value

    init {
        observeDownloadQueueEvents()
    }

    override fun loadDownloadsList(actionOnComplete: (List<InProgressItem>) -> Unit) {
        viewModelScope.launch(listOperationDispatcher) {
            val list = inProgressDao.load().toInProgressList()
            viewModelScope.launch(Dispatchers.Main) {
                actionOnComplete(list)
            }
            observeDownloadState()
        }
    }

    private fun loadDownloadsList() {
        viewModelScope.launch(listOperationDispatcher) {
            val list = inProgressDao.load().toInProgressList()
            viewModelScope.launch(Dispatchers.Main) {
                inProgressListUpdate.send(list)
            }
        }
    }

    override fun startDownload() {
        DownloadQueueManager.start(context)
    }

    override fun pauseDownload() {
        notifyViewDownloadStopped()
        queueEventObserver = null
        DownloadQueueManager.stop(context)
    }

    override fun observeIsDownloading(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        _isDownloading.observe(lifecycleOwner, observer)
    }

    override fun observeIsFetching(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        _isFetching.observe(lifecycleOwner, observer)
    }

    override fun observeVideoDetails(lifecycleOwner: LifecycleOwner, observer: Observer<VideoDetails>) {
        videoDetails.observeSend(lifecycleOwner, observer)
    }

    override fun observeProgress(lifecycleOwner: LifecycleOwner, observer: Observer<ProgressUpdate>) {
        progressUpdate.observeSend(lifecycleOwner, observer)
    }

    override fun observeInProgressListUpdate(lifecycleOwner: LifecycleOwner, observer: Observer<List<InProgressItem>>) {
        inProgressListUpdate.observeSend(lifecycleOwner, observer)
    }

    private fun observeDownloadState() {
        viewModelScope.launch(Dispatchers.Main) {
            DownloadQueueManager.state.observeForever(downloadStateObserver)
        }
    }

    private fun List<DownloadItem>.toInProgressList(): List<InProgressItem> {
        val list = mutableListOf<InProgressItem>()
        forEach {
            val item = InProgressItem(
                    title = "${it.name}.${it.ext}",
                    progress = it.getProgress(),
                    inProgressDownloaded = it.getDownloadedText(),
                    inQueueDownloaded = "${it.getProgress()} ${it.getDownloadedText()}"
            )
            list.add(item)
        }
        return list
    }

    private fun DownloadItem.getDownloadedText(): String {
        return if (size == 0L)
            getDownloaded().formatSize()
        else
            "${getDownloaded().formatSize()} / ${size.formatSize()}"
    }

    private fun DownloadItem.getProgress(): Int? {
        return if (size > 0L) {
            val percent = (getDownloaded().toDouble() / size.toDouble()) * 100.0
            if (percent > 100)
                100
            else
                percent.roundToInt()
        } else
            null
    }

    private fun DownloadItem.getDownloaded(): Long {
        val filename = "$name.$ext"
        val file = File(VideoDownloader.getDownloadFolder(context), filename)
        return file.length()
    }

    private fun Long.formatSize(): String = Formatter.formatFileSize(context, this)

    private fun observeDownloadQueueEvents() = viewModelScope.launch(Dispatchers.Main) {

        val detailsFetch = DownloadQueueManager.getDetailsFetchLiveData(context)
        val videoDownload = DownloadQueueManager.getVideoDownloadLiveData(context)
        val nextDownload = DownloadQueueManager.getNextDownloadLiveData(context)
        queueEventObserver?.let {
            detailsFetch.removeObserver(it)
            videoDownload.removeObserver(it)
            nextDownload.removeObserver(it)
        }
        queueEventObserver = QueueEventObserver()
        queueEventObserver?.let {
            detailsFetch.observeForever(it)
            videoDownload.observeForever(it)
            nextDownload.observeForever(it)
        }
    }

    private fun stopObservingDownloadQueueEvents() = queueEventObserver?.let {
        DownloadQueueManager
                .getQueueLiveData(context)
                .removeObserver(it)
        DownloadQueueManager.getDetailsFetchLiveData(context).removeObserver(it)
        DownloadQueueManager.getVideoDownloadLiveData(context).removeObserver(it)
        DownloadQueueManager.getNextDownloadLiveData(context).removeObserver(it)
    }

    private fun notifyViewDownloadStopped() {
        stopTrackingProgress()
        _isDownloading.postValue(false)
        _isFetching.postValue(false)
    }

    private fun startTrackingProgress() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch(progressTrackingDispatcher) {
            while (_isDownloading.value == true) {
                inProgressDao.first()?.apply {
                    viewModelScope.launch(Dispatchers.Main) {
                        progressUpdate.send(
                                ProgressUpdate(
                                        getProgress(),
                                        getDownloadedText()
                                )
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressTrackingJob?.cancel()
    }

    override fun renameItem(index: Int, newName: String) {
        viewModelScope.launch(listOperationDispatcher) {
            val downloads = inProgressDao.load()
            inProgressDao.delete(downloads)

            if (index < downloads.count() && index >= 0) {
                val item = downloads[index]
                val validated = downloadFileValidator.validateName(newName, item.ext) {
                    downloads.any { item -> item.name == it }
                }
                item.name = validated

                inProgressDao.save(downloads)
                loadDownloadsList()
            }
        }
    }

    override fun deleteItem(index: Int) {
        viewModelScope.launch(listOperationDispatcher) {
            val downloads = inProgressDao.load()
            if (index < downloads.count() && index >= 0) {
                val item = downloads[index]
                inProgressDao.delete(listOf(item))
                loadDownloadsList()
            }
        }
    }

    override fun moveItem(srcIndex: Int, destIndex: Int) {
        viewModelScope.launch(listOperationDispatcher) {
            val downloads = inProgressDao.load().toMutableList()
            if (srcIndex in downloads.indices && destIndex in downloads.indices) {
                inProgressDao.delete(downloads)
                val item = downloads[srcIndex]
                downloads.removeAt(srcIndex)
                downloads.add(destIndex, item)
                downloads.forEachIndexed { index, downloadItem ->
                    downloadItem.uid = index
                }
                inProgressDao.save(downloads)
            }
        }
    }

    override fun onCleared() {
        progressTrackingJob?.cancel()
        stopObservingDownloadQueueEvents()
        DownloadQueueManager.state.removeObserver(downloadStateObserver)
        super.onCleared()
    }

    private inner class QueueEventObserver : Observer<List<WorkInfo>> {

        override fun onChanged(t: List<WorkInfo>?) {
            if (t.isNullOrEmpty())
                return

            val workInfo = t[0]
            val isDetailsFetch = workInfo.tags.contains(DETAILS_FETCH_WORKER)
            val isVideoDownload = workInfo.tags.contains(VIDEO_DOWNLOAD_WORKER)
            val isNextDownload = workInfo.tags.contains(NEXT_DOWNLOAD_WORKER)

            when {
                isDetailsFetch -> {
                    workInfo.apply {
                        this.state.isFinished
                        when (state) {
                            WorkInfo.State.RUNNING -> {
                                loadDownloadsList()
                            }
                            WorkInfo.State.FAILED -> {
                                notifyViewDownloadStopped()
                                loadDownloadsList()
                            }
                            WorkInfo.State.SUCCEEDED -> {
                                outputData.apply {
                                    val hasVideoDetails =
                                            getBoolean(DetailsFetchWorker.HAS_VIDEO_DETAILS, false)
                                    val hasAudioDetails =
                                            getBoolean(DetailsFetchWorker.HAS_AUDIO_DETAILS, false)
                                    if (hasVideoDetails) {
                                        onVideoDetailsFetched()
                                    }
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
                isVideoDownload -> {
                    workInfo.apply {
                        when (state) {
                            WorkInfo.State.RUNNING -> {
                                onStartDownload()
                            }
                            WorkInfo.State.FAILED -> {
                                notifyViewDownloadStopped()
                                loadDownloadsList()
                            }
                            else -> {
                            }
                        }
                    }
                }
                isNextDownload -> {
                    workInfo.apply {
                        when (state) {
                            WorkInfo.State.FAILED -> {
                                notifyViewDownloadStopped()
                                loadDownloadsList()
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }

        private fun onStartDownload() {
            startTrackingProgress()
        }

        private fun onVideoDetailsFetched() {
            viewModelScope.launch(listOperationDispatcher) {
                val file = File(context.filesDir, VIDEO_DETAILS_FILE)
                val fileReader = FileReader(file)
                val details = gson.fromJson(fileReader, VideoDetails::class.java)

                viewModelScope.launch(Dispatchers.Main) {
                    videoDetails.send(details)
                }
            }
        }
    }

    private inner class DownloadStateObserver : Observer<DownloadQueueManager.State> {
        override fun onChanged(t: DownloadQueueManager.State?) {
            val isFetching = t == DownloadQueueManager.State.FETCHING_DETAILS
            val isDownloading = t == DownloadQueueManager.State.DOWNLOADING
            if (isDownloading)
                startTrackingProgress()

            _isDownloading.postValue(isFetching || isDownloading)
            _isFetching.postValue(isFetching)
        }
    }
}