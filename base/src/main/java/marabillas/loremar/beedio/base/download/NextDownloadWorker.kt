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
import marabillas.loremar.beedio.base.database.CompletedItem
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.database.InactiveItem
import java.io.File

class NextDownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val downloadsDB = Room.databaseBuilder(
                    context,
                    DownloadListDatabase::class.java,
                    "downloads"
            )
            .build()
    private val downloadList = downloadsDB.downloadListDao()
    private val completedList = downloadsDB.completedListDao()
    private val inactiveList = downloadsDB.inactiveListDao()

    override fun doWork(): Result {
        val completed = inputData.getBoolean(VideoDownloadWorker.DOWNLOAD_COMPLETED, false)
        val list = downloadList.load().toMutableList()

        if (completed) {
            val filename = "${list[0].name}.${list[0].ext}"
            val file = File(VideoDownloader.getDownloadFolder(applicationContext), filename)
            val items = completedList.load().toMutableList()
            completedList.delete(items)
            items.add(CompletedItem(0, file.absolutePath))
            items.forEachIndexed { i, item -> item.uid = i }
            completedList.save(items)
        } else {
            val items = inactiveList.load().toMutableList()
            inactiveList.delete(items)
            list[0].copy()
            items.add(InactiveItem(
                    uid = 0,
                    name = list[0].name,
                    videoUrl = list[0].videoUrl,
                    ext = list[0].ext,
                    size = list[0].size,
                    sourceWebpage = list[0].sourceWebpage,
                    sourceWebsite = list[0].sourceWebsite,
                    isChunked = list[0].isChunked,
                    audioUrl = list[0].audioUrl,
                    isAudioChunked = list[0].isAudioChunked
            ))
            items.forEachIndexed { i, item -> item.uid = i }
            inactiveList.save(items)
        }

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