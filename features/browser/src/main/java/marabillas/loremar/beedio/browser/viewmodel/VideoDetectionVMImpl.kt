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

package marabillas.loremar.beedio.browser.viewmodel

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import marabillas.loremar.beedio.base.database.DownloadItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.DownloadFileValidator
import marabillas.loremar.beedio.base.download.DownloadQueueManager
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.media.VideoDetailsFetcher
import marabillas.loremar.beedio.base.mvvm.SendLiveData
import marabillas.loremar.beedio.base.web.HttpNetwork
import timber.log.Timber
import java.net.URL
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.pow

class VideoDetectionVMImpl(private val context: Context) : VideoDetectionVM() {
    override val foundVideos: List<FoundVideo>
        get() = _foundVideos

    private val _foundVideos = mutableListOf<FoundVideo>()
    private val filters = arrayOf("mp4", "video", "googleusercontent", "embed")
    private val network = HttpNetwork()
    private val detailsFetcher = VideoDetailsFetcher()
    private val downloadFileValidator = DownloadFileValidator(context)
    private val analyzeDispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            .asCoroutineDispatcher()
    private val detailsFetcherThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val downloadStarterThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val downloads = Room
            .databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
            .downloadListDao()

    private val sendFoundVideo = SendLiveData<FoundVideo>()
    private val isAnalyzing = MutableLiveData<Boolean>()

    private var analysisCount = 0

    init {
        isAnalyzing.value = false
    }

    override fun analyzeUrlForVideo(url: String, title: String, sourceWebPage: String) {
        viewModelScope.launch(analyzeDispatcher) {
            onStartAnalysis()

            filter(url) {
                Timber.i("Analyzing $url")
                connect(url) {
                    if (contentType().containsVideoOrAudio())
                        extractVideo(title, sourceWebPage)
                    else if (contentType().isM3U8())
                        extractM3U8Video(title, sourceWebPage)
                }
            }

            onEndAnalysis()
        }
    }

    private fun onStartAnalysis() {
        analysisCount++
        if (analysisCount == 1)
            isAnalyzing.postValue(true)
    }

    private fun onEndAnalysis() {
        analysisCount--
        if (analysisCount == 0)
            isAnalyzing.postValue(false)
    }

    private fun filter(url: String, doIfTrue: (String) -> Unit) {
        for (filter in filters)
            if (url.contains(filter, true))
                doIfTrue(url)
    }

    private fun HttpNetwork.Connection.contentType() = getResponseHeader("Content-Type")?.toLowerCase(Locale.US)
            ?: ""

    private fun String.containsVideoOrAudio() = contains("video") || contains("audio")

    private fun String.isM3U8() = equals("application/x-mpegurl") || equals("application/vnd.apple.mpegurl")

    private fun HttpNetwork.Connection.extractVideo(
            title: String,
            sourceWebPage: String
    ) {
        val host = URL(sourceWebPage).host
        val contentType = contentType()

        if (host.contains("twitter.com") && contentType == "video/mp2t")
            return

        var url = getResponseHeader("Location") ?: urlHandler.url ?: return
        var size = getResponseHeader("Content-Length") ?: "0"
        var name = when {
            title.isNotBlank() -> title
            contentType.contains("audio") -> "audio"
            else -> "video"
        }
        var sourceWebsite = ""
        var isChunked = false

        if (host.contains("youtube.com") || urlHandler.host?.contains("googlevideo.com") == true) {
            url = url.substringBeforeLast("&range").also {
                connect(it) {
                    size = getResponseHeader("Content-Length") ?: "0"
                    name = getYoutubeVideoTitle(sourceWebPage)
                }
            }
        } else if (host.contains("dailymotion.com")) {
            isChunked = true
            sourceWebsite = "dailymotion.com"
            url = url.replace("(frag\\()+(\\d+)+(\\))", "FRAGMENT")
            size = "0"
        } else if (host.contains("vimeo.com") && url.endsWith("m4s")) {
            isChunked = true
            sourceWebsite = "vimeo.com"
            url = url.replace("(segment-)+(\\d+)", "SEGMENT")
            size = "0"
        } else if (host.contains("facebook.com") && url.contains("bytestart")) {
            url = "https://video.xx.fbcdn${url.substringAfter("fbcdn").substringBeforeLast("&bytestart")}"
            sourceWebsite = "facebook.com"
            connect(url) {
                size = getResponseHeader("Content-Length") ?: "0"
            }
        }

        val ext = getExtensionFor(contentType)

        FoundVideo(
                name = name,
                url = url,
                ext = ext,
                size = size,
                sourceWebPage = sourceWebPage,
                sourceWebsite = sourceWebsite,
                isChunked = isChunked
        ).apply { onFoundVideo(this) }
    }

    private fun getYoutubeVideoTitle(sourcePage: String): String {
        var title = ""
        connect("https://www.youtube.com/oembed?url=$sourcePage&format=json") {
            val json = content
            val jsonObject = JsonParser.parseString(json).asJsonObject
            title = jsonObject.get("title").asString
        }
        return title
    }

    private fun getExtensionFor(contentType: String): String {
        return when (contentType) {
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            "video/mp2t" -> "ts"
            "audio/webm" -> "webm"
            else -> if (contentType.contains("audio")) "m4a" else "mp4"
        }
    }

    private fun HttpNetwork.Connection.extractM3U8Video(
            title: String,
            sourceWebPage: String
    ) {

        val host = URL(sourceWebPage).host
        if (host.contains("twitter.com") || host.contains("metacafe.com")
                || host.contains("myspace.com")) {

            val name = if (title.isNotBlank()) title else "video"

            var prefix = ""
            var ext = "mp4"
            var sourceWebsite = ""
            when {
                host.contains("twitter.com") -> {
                    prefix = "https://video.twimg.com"
                    ext = "ts"
                    sourceWebsite = "twitter.com"
                }
                host.contains("metacafe.com") -> {
                    val url = urlHandler.url ?: return
                    prefix = "${url.substringBeforeLast("/")}/"
                    sourceWebsite = "metacafe.com"
                    ext = "mp4"
                }
                host.contains("myspace.com") -> {
                    FoundVideo(
                            name = name,
                            url = urlHandler.url ?: return,
                            ext = "ts",
                            size = "0",
                            sourceWebPage = sourceWebPage,
                            sourceWebsite = "myspace.com",
                            isChunked = true
                    ).apply { onFoundVideo(this) }
                    return
                }
            }
            stream?.reader()?.forEachLine {
                if (it.endsWith(".m3u8")) {
                    val url = "$prefix$it"
                    FoundVideo(
                            name = name,
                            url = url,
                            ext = ext,
                            size = "0",
                            sourceWebPage = sourceWebPage,
                            sourceWebsite = sourceWebsite,
                            isChunked = true
                    ).apply { onFoundVideo(this) }
                }
            }
        }
    }

    private fun connect(url: String, block: HttpNetwork.Connection.() -> Unit) =
            try {
                network.open(url).apply(block)
            } catch (e: Exception) {
                Timber.e(e, "Failed connecting to $url")
                null
            }

    private fun onFoundVideo(video: FoundVideo) {
        Timber.i("""
                    FoundVideo:
                        url = ${video.url}
                        name = ${video.name}
                        ext = ${video.ext}
                        size = ${video.size}
                        page = ${video.sourceWebPage}
                        site = ${video.sourceWebsite}
                        chunked = ${if (video.isChunked) "yes" else "no"}                    
                """.trimIndent())

        video.name = downloadFileValidator.validateName(video.name, video.ext, this::checkIfAlreadyExists)
        _foundVideos.add(video)

        viewModelScope.launch(Dispatchers.Main) {
            sendFoundVideo.send(video)
        }
    }

    override fun observeIsAnalyzing(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>) {
        isAnalyzing.observe(lifecycleOwner, observer)
    }

    override fun receiveForFoundVideo(lifecycleOwner: LifecycleOwner, observer: Observer<FoundVideo>) {
        sendFoundVideo.observeSend(lifecycleOwner, observer)
    }

    override fun selectAll() {
        _foundVideos.forEach { it.isSelected = true }
    }

    override fun unselectAll() {
        _foundVideos.forEach { it.isSelected = false }
    }

    override fun setSelection(index: Int, isSelected: Boolean) {
        _foundVideos[index].isSelected = isSelected
    }

    override fun deleteItem(index: Int) {
        _foundVideos.removeAt(index)
    }

    override fun deleteAllSelected() {
        _foundVideos.removeAll { it.isSelected }
    }

    override fun renameItem(index: Int, newName: String) {
        val video = _foundVideos[index]
        val validated = downloadFileValidator.validateName(newName, video.ext, this::checkIfAlreadyExists)
        video.name = validated
    }

    override fun fetchDetails(index: Int, fetchListener: VideoDetailsFetcher.FetchListener) {
        viewModelScope.launch(detailsFetcherThread) {
            _foundVideos[index].isFetchingDetails = true
            detailsFetcher.fetchDetails(_foundVideos[index].url, object : VideoDetailsFetcher.FetchListener {
                override fun onUnFetched(error: Throwable) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _foundVideos[index].isFetchingDetails = false
                        fetchListener.onUnFetched(error)
                    }
                }

                override fun onFetched(details: VideoDetails) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _foundVideos[index].isFetchingDetails = false
                        _foundVideos[index].details = details
                        fetchListener.onFetched(details)
                    }
                }
            })
        }
    }

    override fun closeDetailsFetcher() {
        detailsFetcher.close()
    }

    override fun download(index: Int) {
        viewModelScope.launch(downloadStarterThread) {
            val list = downloads.load().toMutableList()
            downloads.delete(list)
            val item = _foundVideos[index]
            deleteItem(index)
            val new = DownloadItem(
                    uid = 0,
                    name = item.name,
                    videoUrl = item.url,
                    ext = item.ext,
                    size = item.size.toLong(),
                    sourceWebsite = item.sourceWebsite,
                    sourceWebpage = item.sourceWebPage,
                    isChunked = item.isChunked
            )
            list.add(0, new)
            list.forEachIndexed { i, it -> it.uid = i }
            downloads.save(list)

            viewModelScope.launch(Dispatchers.Main) {
                DownloadQueueManager.start(context)
            }
        }
    }

    override fun queueAllSelected(doOnComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = downloads.load().toMutableList()
            downloads.delete(list)
            _foundVideos.filter { it.isSelected }.forEach {
                val new = DownloadItem(
                        uid = 0,
                        name = it.name,
                        videoUrl = it.url,
                        ext = it.ext,
                        size = it.size.toLong(),
                        sourceWebsite = it.sourceWebsite,
                        sourceWebpage = it.sourceWebPage,
                        isChunked = it.isChunked,
                        audioUrl = it.audioUrl,
                        isAudioChunked = it.isAudioChunked
                )
                list.add(new)
            }
            list.forEachIndexed { i, it -> it.uid = i }
            downloads.save(list)

            viewModelScope.launch(Dispatchers.Main) {
                doOnComplete()
            }
        }
    }

    override fun mergeSelected(): Boolean {
        val selected = _foundVideos.filter { it.isSelected }
        if (selected.count() != 2)
            return false

        if (selected.component1().details == null || selected.component2().details == null)
            return false

        if (selected.component1().details?.duration != selected.component2().details?.duration)
            return false

        var firstHasVideoOnly = false;
        var firstHasAudioOnly = false
        var secondHasVideoOnly = false;
        var secondHasAudioOnly = false

        selected.component1().details?.let {
            firstHasVideoOnly = it.vcodec != null && it.acodec == null
            firstHasAudioOnly = it.acodec != null && it.vcodec == null
        }
        selected.component2().details?.let {
            secondHasVideoOnly = it.vcodec != null && it.acodec == null
            secondHasAudioOnly = it.acodec != null && it.vcodec == null
        }

        val isVideoAudio = firstHasVideoOnly && secondHasAudioOnly
        val isAudioVideo = firstHasAudioOnly && secondHasVideoOnly
        if (!isVideoAudio && !isAudioVideo)
            return false

        val merge: (FoundVideo, FoundVideo) -> Unit = { vid, aud ->
            vid.audioUrl = aud.url
            vid.isAudioChunked = aud.isChunked
            vid.size = (vid.size.toLong() + aud.size.toLong()).toString()
            vid.details?.apply {
                acodec = aud.details?.acodec
                filesize = vid.size.formatFilesize()
            }
            _foundVideos.remove(aud)
        }

        if (isVideoAudio)
            merge(selected.component1(), selected.component2())
        else
            merge(selected.component2(), selected.component1())

        return true
    }

    private fun String.formatFilesize(): String? {
        return try {
            val size: Long = toLong()
            if (size <= 0) return "0"
            val units = arrayOf("B", "kB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
                    .toString() + " " + units[digitGroups]
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun checkIfAlreadyExists(name: String) = _foundVideos.any { it.name == name }
}