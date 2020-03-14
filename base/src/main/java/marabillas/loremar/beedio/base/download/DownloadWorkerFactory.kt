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
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import marabillas.loremar.beedio.base.database.DownloadListDatabase

class DownloadWorkerFactory(private val downloadDB: DownloadListDatabase) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return when (workerClassName) {
            DetailsFetchWorker::class.java.name -> DetailsFetchWorker(appContext, workerParameters, downloadDB)
            VideoDownloadWorker::class.java.name -> VideoDownloadWorker(appContext, workerParameters, downloadDB)
            NextDownloadWorker::class.java.name -> NextDownloadWorker(appContext, workerParameters, downloadDB)
            else -> throw IllegalArgumentException("Invalid Worker class name")
        }
    }
}