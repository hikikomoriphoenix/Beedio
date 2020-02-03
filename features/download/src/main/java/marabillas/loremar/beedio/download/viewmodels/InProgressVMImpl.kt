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
import androidx.room.Room
import androidx.work.WorkInfo
import kotlinx.coroutines.*
import marabillas.loremar.beedio.base.database.DownloadItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.DownloadQueueWorker
import marabillas.loremar.beedio.base.download.DownloadQueueWorker.Companion.QUEUE_DOWNLOAD_START
import marabillas.loremar.beedio.base.download.DownloadQueueWorker.Companion.QUEUE_EVENT
import marabillas.loremar.beedio.base.download.DownloadQueueWorker.Companion.QUEUE_FINISHED
import marabillas.loremar.beedio.base.download.DownloadQueueWorker.Companion.QUEUE_START_NEW
import marabillas.loremar.beedio.base.download.VideoDownloader
import marabillas.loremar.beedio.base.mvvm.SendLiveData
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class InProgressVMImpl(private val context: Context) : InProgressVM() {

    private val listOperationDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val progressTrackingDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private var progressTrackingJob: Job? = null

    private val downloadsDB = Room
            .databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
            .downloadListDao()

    private var inProgressList = mutableListOf<InProgressItem>()
    private var queueEventObserver: QueueEventObserver? = null

    private val progressUpdate = SendLiveData<ProgressUpdate>()
    private val inProgressListUpdate = SendLiveData<List<InProgressItem>>()
    private val _isDownloading = MutableLiveData<Boolean>()
    private val _isFetching = MutableLiveData<Boolean>()

    override val isDownloading: Boolean?
        get() = _isDownloading.value

    override fun loadDownloadsList(actionOnComplete: (List<InProgressItem>) -> Unit) {
        viewModelScope.launch(listOperationDispatcher) {
            downloadsDB.load().toInProgressList()
            actionOnComplete(inProgressList)
            checkDownloadStatus()
            observeDownloadQueueEvents()
        }
    }

    private fun loadDownloadsList() {
        viewModelScope.launch(listOperationDispatcher) {
            downloadsDB.load().toInProgressList()
            viewModelScope.launch(Dispatchers.Main) {
                inProgressListUpdate.send(inProgressList)
                checkDownloadStatus()
            }
        }
    }

    override fun startDownload() {
        DownloadQueueWorker.work(context)
        observeDownloadQueueEvents()
    }

    override fun pauseDownload() {
        notifyViewDownloadStopped()
        DownloadQueueWorker.stop(context)
    }

    override fun observeIsDownloading(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        _isDownloading.observe(lifecycleOwner, observer)
    }

    override fun observeIsFetching(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        _isFetching.observe(lifecycleOwner, observer)
    }

    override fun observeProgress(lifecycleOwner: LifecycleOwner, observer: Observer<ProgressUpdate>) {
        progressUpdate.observeSend(lifecycleOwner, observer)
    }

    override fun observeInProgressListUpdate(lifecycleOwner: LifecycleOwner, observer: Observer<List<InProgressItem>>) {
        inProgressListUpdate.observeSend(lifecycleOwner, observer)
    }

    private fun checkDownloadStatus() {
        val downloadStatus = DownloadQueueWorker.status
        val isFetching = downloadStatus == DownloadQueueWorker.Status.FETCHING_DETAILS
        val isDownloading = downloadStatus == DownloadQueueWorker.Status.DOWNLOADING
        if (isDownloading)
            startTrackingProgress()

        _isDownloading.postValue(isFetching || isDownloading)
        _isFetching.postValue(isFetching)
    }

    private fun List<DownloadItem>.toInProgressList() {
        inProgressList.clear()
        forEach {
            val item = InProgressItem(
                    title = "${it.name}.${it.ext}",
                    progress = it.getProgress(),
                    inProgressDownloaded = it.getDownloadedText(),
                    inQueueDownloaded = "${it.getProgress()} ${it.getDownloadedText()}"
            )
            inProgressList.add(item)
        }
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
        if (queueEventObserver == null) {
            queueEventObserver = QueueEventObserver().apply {
                DownloadQueueWorker.getQueueEventLiveData(context).observeForever(this)
            }
        }
    }

    private fun stopObservingDownloadQueueEvents() = queueEventObserver?.let {
        DownloadQueueWorker
                .getQueueEventLiveData(context)
                .removeObserver(it)
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
                downloadsDB.first()?.apply {
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

    override fun onCleared() {
        progressTrackingJob?.cancel()
        stopObservingDownloadQueueEvents()
        super.onCleared()
    }

    inner class QueueEventObserver : Observer<List<WorkInfo>> {
        override fun onChanged(t: List<WorkInfo>?) {
            if (t.isNullOrEmpty())
                return
            t[0].apply {
                when (progress.getInt(QUEUE_EVENT, -1)) {
                    QUEUE_START_NEW -> {
                        _isDownloading.postValue(true)
                        loadDownloadsList()
                    }
                    QUEUE_FINISHED -> {
                        notifyViewDownloadStopped()
                        loadDownloadsList()
                    }
                    QUEUE_DOWNLOAD_START -> {
                        _isFetching.postValue(false)
                        startTrackingProgress()
                    }
                }
            }
        }
    }
}