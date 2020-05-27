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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marabillas.loremar.beedio.base.database.DownloadItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.DownloadQueueManager
import marabillas.loremar.beedio.base.download.VideoDownloader
import marabillas.loremar.beedio.base.web.HttpNetwork
import java.io.File
import java.net.URL
import java.util.*
import kotlin.math.roundToInt

class InactiveVMImpl(private val context: Context, downloadDB: DownloadListDatabase) : InactiveVM() {
    private val inactiveDao = downloadDB.inactiveListDao()
    private val inProgressDao = downloadDB.downloadListDao()

    private val network = HttpNetwork()
    private val filters = arrayOf("mp4", "video", "googleusercontent", "embed")

    override fun loadList(actionOnComplete: (List<InactiveItem>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val inactiveList = inactiveDao.load().toInactiveList()
            viewModelScope.launch(Dispatchers.Main) {
                actionOnComplete(inactiveList)
            }
        }
    }

    private fun List<marabillas.loremar.beedio.base.database.InactiveItem>.toInactiveList()
            : List<InactiveItem> {

        val list = mutableListOf<InactiveItem>()
        forEach {
            val item = InactiveItem(
                    filename = "${it.name}.${it.ext}",
                    downloaded = "${it.getProgress()} ${it.getDownloadedText()}",
                    sourceWebpage = it.sourceWebpage,
                    size = it.size
            )
            list.add(item)
        }
        return list
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getDownloadedText(): String {
        return if (size == 0L)
            getDownloaded().formatSize()
        else
            "${getDownloaded().formatSize()} / ${size.formatSize()}"
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getProgress(): Int? {
        return if (size > 0L) {
            val percent = (getDownloaded().toDouble() / size.toDouble()) * 100.0
            if (percent > 100)
                100
            else
                percent.roundToInt()
        } else
            null
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getDownloaded(): Long {
        val filename = "$name.$ext"
        val file = File(VideoDownloader.getDownloadFolder(context), filename)
        return file.length()
    }

    private fun Long.formatSize(): String = Formatter.formatFileSize(context, this)

    override fun deleteItem(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = inactiveDao.load()[index]
            inactiveDao.delete(listOf(item))
        }
    }

    override fun clearList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = inactiveDao.load()
            inactiveDao.delete(list)
        }
    }

    override fun getInactiveItemSourcePage(index: Int): String = inactiveDao.get(index).sourceWebpage

    override fun analyzeUrlForFreshLink(url: String, index: Int, onFound: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            filter(url) {
                connect(url) {
                    if (contentType().containsVideoOrAudio())
                        identifyCorrectUrlBySize(index, onFound)
                }
            }
        }
    }

    private suspend fun filter(url: String, doIfTrue: suspend (String) -> Unit) {
        for (filter in filters)
            if (url.contains(filter, true))
                doIfTrue(url)
    }

    private suspend fun HttpNetwork.Connection.contentType() = getResponseHeader("Content-Type")?.toLowerCase(Locale.US)
            ?: ""

    private fun String.containsVideoOrAudio() = contains("video") || contains("audio")

    private suspend fun HttpNetwork.Connection.identifyCorrectUrlBySize(index: Int,
                                                                        onFound: suspend () -> Unit) {

        val inactiveItem = inactiveDao.get(index)
        val host = URL(inactiveItem.sourceWebpage).host
        val contentType = contentType()

        if (host.contains("twitter.com") && contentType == "video/mp2t")
            return

        var url = getResponseHeader("Location") ?: urlHandler.url ?: return
        var analyzedSize = getResponseHeader("Content-Length") ?: "0"

        if (host.contains("youtube.com") || urlHandler.host?.contains("googlevideo.com") == true) {
            url.substringBeforeLast("&range").also {
                connect(it) {
                    analyzedSize = getResponseHeader("Content-Length") ?: "0"
                }
            }
        } else if (host.contains("facebook.com") && url.contains("bytestart")) {
            url = "https://video.xx.fbcdn${url.substringAfter("fbcdn").substringBeforeLast("&bytestart")}"
            connect(url) {
                analyzedSize = getResponseHeader("Content-Length") ?: "0"
            }
        }

        if (inactiveItem.size == analyzedSize.toLong()) {
            refreshLink(index, url)

            withContext(Dispatchers.Main) {
                onFound()
            }
        }
    }

    private suspend fun connect(url: String, block: suspend HttpNetwork.Connection.() -> Unit) =
            try {
                network.open(url).apply { block() }
            } catch (e: Exception) {
                null
            }

    private fun refreshLink(index: Int, freshLink: String) {
        DownloadQueueManager.stop(context)
        val downloads = inProgressDao.load().toMutableList()
        val inactiveItem = inactiveDao.get(index)

        val freshItem = DownloadItem(
                uid = 0,
                videoUrl = freshLink,
                ext = inactiveItem.ext,
                name = inactiveItem.name,
                size = inactiveItem.size,
                sourceWebpage = inactiveItem.sourceWebpage,
                sourceWebsite = inactiveItem.sourceWebsite,
                isChunked = inactiveItem.isChunked,
                audioUrl = inactiveItem.audioUrl,
                isAudioChunked = inactiveItem.isAudioChunked
        )

        inProgressDao.delete(downloads)
        downloads.add(0, freshItem)
        downloads.forEachIndexed { i, item -> item.uid = i }
        inProgressDao.save(downloads)

        val inactives = inactiveDao.load().toMutableList()
        inactiveDao.delete(inactives)
        inactives.removeAt(index)
        inactives.forEachIndexed { i, item -> item.uid = i }
        inactiveDao.save(inactives)

        DownloadQueueManager.start(context)
    }
}