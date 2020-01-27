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

package marabillas.loremar.beedio.base.download

import android.content.Context
import androidx.room.Room
import androidx.work.*
import com.google.gson.Gson
import marabillas.loremar.beedio.base.database.DownloadItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.DOWNLOAD_EXCEPTION_FILE
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_AUDIO_URL
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_EXT
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_IS_CHUNKED
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_NAME
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_SIZE
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_SOURCE_WEBSITE
import marabillas.loremar.beedio.base.download.VideoDownloader.Companion.KEY_URL
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.media.VideoDetailsFetcher
import java.io.File

class DownloadQueueWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val downloadList = Room
            .databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
            .downloadListDao()

    private val videoDetailsFetcher = VideoDetailsFetcher()
    private val gson = Gson()

    companion object {
        const val QUEUE_EVENT = "queue_event"
        const val QUEUE_EVENT_DATA_FILE = "queue_event_data.json"
        const val QUEUE_START_NEW = 0
        const val QUEUE_VIDEO_DETAILS = 1
        const val QUEUE_AUDIO_DETAILS = 2
        const val QUEUE_DOWNLOAD_START = 3
        const val QUEUE_EXCEPTION = 4
    }

    override fun doWork(): Result {
        processTopItem()
        return Result.success()
    }

    private fun processTopItem() {
        val first = downloadList.first() ?: return

        println("FIRST = $first")

        update(QUEUE_START_NEW)

        videoDetailsFetcher.fetchDetails(first.videoUrl, object : VideoDetailsFetcher.FetchListener {
            override fun onUnFetched(error: Throwable) {
                startDownload(first)
            }

            override fun onFetched(details: VideoDetails) {
                update(QUEUE_VIDEO_DETAILS, details)

                if (first.audioUrl != null)
                    videoDetailsFetcher.fetchDetails(first.audioUrl, object : VideoDetailsFetcher.FetchListener {
                        override fun onUnFetched(error: Throwable) {
                            startDownload(first)
                        }

                        override fun onFetched(details: VideoDetails) {
                            update(QUEUE_AUDIO_DETAILS, details)
                            startDownload(first)
                        }
                    })
                else
                    startDownload(first)
            }
        })

    }

    private fun startDownload(item: DownloadItem) {
        update(QUEUE_DOWNLOAD_START)

        val input = workDataOf(
                KEY_NAME to item.name,
                KEY_URL to item.videoUrl,
                KEY_EXT to item.ext,
                KEY_SIZE to item.size,
                KEY_SOURCE_WEBSITE to item.sourceWebsite,
                KEY_IS_CHUNKED to item.isChunked,
                KEY_AUDIO_URL to item.audioUrl
        )
        val downloadRequest = OneTimeWorkRequestBuilder<VideoDownloader>()
                .setInputData(input)
                .build()

        val workMngr = WorkManager.getInstance(applicationContext)

        workMngr.getWorkInfoById(downloadRequest.id).apply {
            addListener(
                    {
                        get().apply {
                            if (this != null && state == WorkInfo.State.SUCCEEDED) {
                                completed()
                                next()
                            } else if (this != null && state == WorkInfo.State.FAILED) {
                                failed()
                                next()
                            }
                        }
                    },
                    {
                        it.run()
                    }
            )
        }

        workMngr.enqueue(downloadRequest)
    }

    private fun completed() {
        // TODO
    }

    private fun next() {
        val list = downloadList.load().toMutableList()
        downloadList.delete(list)
        list.removeAt(0)
        list.forEachIndexed { i, item -> item.uid = i }
        downloadList.save(list)
        processTopItem()
    }

    private fun failed() {
        val file = File(applicationContext.cacheDir, DOWNLOAD_EXCEPTION_FILE)
        val e = gson.fromJson(file.bufferedReader(),
                VideoDownloader.DownloadException::class.java)

        if (e.cause is VideoDownloader.UnavailableException) {
            // TODO Add to inactive list
        } else {
            update(QUEUE_EXCEPTION, e)
        }
    }

    private fun update(event: Int, data: Any? = null) {
        if (data != null) {
            val json = gson.toJson(data)
            val file = File(applicationContext.cacheDir, QUEUE_EVENT_DATA_FILE)
            file.writeText(json)
        }
        val workData = workDataOf(QUEUE_EVENT to event)
        setProgressAsync(workData)
    }
}