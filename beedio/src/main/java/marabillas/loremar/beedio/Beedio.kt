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

package marabillas.loremar.beedio

import android.content.Context
import androidx.multidex.MultiDex
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.DownloadWorkerFactory

class Beedio : DaggerApplication(), Configuration.Provider {
    private val downloadDB by lazy {
        Room.databaseBuilder(
                        this,
                        DownloadListDatabase::class.java,
                        "downloads"
                )
                .build()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerBeedioComponent.factory().create(this, downloadDB)

    override fun getWorkManagerConfiguration(): Configuration {
        val workerFactory = DownloadWorkerFactory(downloadDB)
        return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
    }
}