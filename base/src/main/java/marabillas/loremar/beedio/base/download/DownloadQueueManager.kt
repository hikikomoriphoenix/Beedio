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
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File

class DownloadQueueManager {
    enum class State { INACTIVE, FETCHING_DETAILS, DOWNLOADING }

    companion object {
        var state = MutableLiveData<State>()
        const val VIDEO_DETAILS_FILE = "queue_video_details.json"
        const val AUDIO_DETAILS_FILE = "queue_audio_details.json"
        const val DETAILS_FETCH_WORKER = "tag_details_fetch"
        const val VIDEO_DOWNLOAD_WORKER = "tag_video_download"
        const val NEXT_DOWNLOAD_WORKER = "tag_next_download"

        private const val UNIQUE_NAME = "download_queue"

        init {
            state.value = State.INACTIVE
        }

        fun start(context: Context) {
            val fetchDetails = OneTimeWorkRequestBuilder<DetailsFetchWorker>()
                    .addTag(DETAILS_FETCH_WORKER)
                    .build()
            val startDownload = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
                    .addTag(VIDEO_DOWNLOAD_WORKER)
                    .build()
            val nextDownload = OneTimeWorkRequestBuilder<NextDownloadWorker>()
                    .addTag(NEXT_DOWNLOAD_WORKER)
                    .build()

            WorkManager.getInstance(context)
                    .beginUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, fetchDetails)
                    .then(startDownload)
                    .then(nextDownload)
                    .enqueue()
        }

        fun getQueueLiveData(context: Context) = WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(UNIQUE_NAME)

        fun getDetailsFetchLiveData(context: Context) = WorkManager
                .getInstance(context)
                .getWorkInfosByTagLiveData(DETAILS_FETCH_WORKER)

        fun getVideoDownloadLiveData(context: Context) = WorkManager
                .getInstance(context)
                .getWorkInfosByTagLiveData(VIDEO_DOWNLOAD_WORKER)

        fun getNextDownloadLiveData(context: Context) = WorkManager
                .getInstance(context)
                .getWorkInfosByTagLiveData(NEXT_DOWNLOAD_WORKER)

        fun stop(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)

        fun loadData(context: Context, filename: String): String? {
            val file = File(context.filesDir, filename)
            return if (file.exists())
                file.readText()
            else
                null
        }

        fun deleteData(context: Context, filename: String) {
            val file = File(context.filesDir, filename)
            if (file.exists())
                file.delete()
        }
    }
}