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
import marabillas.loremar.beedio.base.database.DownloadListDatabase

class NextDownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val downloadList = Room
            .databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
            .downloadListDao()

    override fun doWork(): Result {
        val completed = inputData.getBoolean(VideoDownloadWorker.DOWNLOAD_COMPLETED, false)

        // TODO COMPLETED AND FAILED


        val list = downloadList.load().toMutableList()
        downloadList.delete(list)
        list.removeAt(0)
        list.forEachIndexed { i, item -> item.uid = i }
        downloadList.save(list)

        DetailsFetchWorker.deleteDetailsFiles(applicationContext)
        return if (downloadList.first() != null) {
            DownloadQueueManager.start(applicationContext)
            Result.success()
        } else {
            DownloadQueueManager.state.postValue(DownloadQueueManager.State.INACTIVE)
            Result.failure()
        }
    }
}