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

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.mvvm.ObservableViewModel

abstract class DownloadVM : ObservableViewModel() {
    abstract fun setSelectedNavItem(id: Int)
    abstract fun observeSelectedNavItem(lifecycleOwner: LifecycleOwner, observer: Observer<Int>)
}

abstract class InProgressVM : ObservableViewModel() {
    abstract val isDownloading: Boolean?
    abstract fun loadDownloadsList(actionOnComplete: (List<InProgressItem>) -> Unit)
    abstract fun startDownload()
    abstract fun pauseDownload()
    abstract fun observeIsDownloading(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>)
    abstract fun observeIsFetching(lifecycleOwner: LifecycleOwner, observer: Observer<Boolean>)
    abstract fun observeVideoDetails(lifecycleOwner: LifecycleOwner, observer: Observer<VideoDetails>)
    abstract fun observeProgress(lifecycleOwner: LifecycleOwner, observer: Observer<ProgressUpdate>)
    abstract fun observeInProgressListUpdate(lifecycleOwner: LifecycleOwner, observer: Observer<List<InProgressItem>>)
    abstract fun renameItem(index: Int, newName: String)
    abstract fun deleteItem(index: Int)
    abstract fun moveItem(srcIndex: Int, destIndex: Int)

    data class InProgressItem(
            val title: String,
            var thumbnail: Bitmap? = null,
            var progress: Int? = null,
            var inProgressDownloaded: String = "0KB",
            var inQueueDownloaded: String = "0% 0KB"
    )

    data class ProgressUpdate(val progress: Int?, val downloaded: String)
}

abstract class CompletedVM : ObservableViewModel() {
    abstract fun loadList(actionOnComplete: (List<String>) -> Unit)
    abstract fun observeItemDetailsFetched(
            lifecycleOwner: LifecycleOwner,
            observer: Observer<CompletedItemMiniDetails>)
    abstract fun deleteItem(index: Int)
    abstract fun clearList()

    data class CompletedItemMiniDetails(
            val index: Int,
            val thumbnail: Bitmap? = null,
            val duration: String
    )
}