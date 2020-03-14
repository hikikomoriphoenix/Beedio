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
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import java.io.File

class VideoDownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val videoDownloader = VideoDownloader(context)
    private var videoDownloadJob: Job? = null
    private val gson = Gson()

    companion object {
        const val DOWNLOAD_COMPLETED = "download_completed"
    }

    private val downloadList = Room
            .databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
            .downloadListDao()

    override fun doWork(): Result {
        DownloadQueueManager.state.postValue(DownloadQueueManager.State.DOWNLOADING)
        val first = downloadList.first() ?: return Result.failure().apply {
            DetailsFetchWorker.deleteDetailsFiles(applicationContext)
            DownloadQueueManager.state.postValue(DownloadQueueManager.State.INACTIVE)
        }

        var data = workDataOf(DOWNLOAD_COMPLETED to false)
        runBlocking {
            videoDownloadJob = launch {
                try {
                    videoDownloader.download(first)
                    data = workDataOf(DOWNLOAD_COMPLETED to true)
                } catch (e: VideoDownloader.DownloadException) {
                    e.save()
                }
            }
            videoDownloadJob?.join()
        }

        videoDownloader.stop()

        return Result.success(data)
    }

    private fun VideoDownloader.DownloadException.save() {
        val json = gson.toJson(this)
        File(applicationContext.filesDir, "queue_download_exception.json").writeText(json)
    }

    override fun onStopped() {
        super.onStopped()
        videoDownloader.stop()
        videoDownloadJob?.cancel()
    }
}