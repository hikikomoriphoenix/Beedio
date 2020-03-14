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

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.media.VideoDetails
import marabillas.loremar.beedio.base.media.VideoDetailsFetcher
import marabillas.loremar.beedio.base.mvvm.SendLiveData
import java.io.File

class CompletedVMImpl(downloadDB: DownloadListDatabase) : CompletedVM() {

    private val detailsFetcher = VideoDetailsFetcher()

    private val itemDetailsFetched = SendLiveData<CompletedItemMiniDetails>()

    private val completedDao = downloadDB.completedListDao()

    override fun loadList(actionOnComplete: (List<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list: List<String> = completedDao.load().map {
                File(it.filepath).name
            }
            viewModelScope.launch(Dispatchers.Main) {
                actionOnComplete(list)
                fetchDetails()
            }
        }
    }

    private fun fetchDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            completedDao.load().forEachIndexed { i, it ->
                detailsFetcher.fetchMiniDetails(it.filepath, object : VideoDetailsFetcher.FetchListener {
                    override fun onUnFetched(error: Throwable) {
                        TODO("Not yet implemented")
                    }

                    override fun onFetched(details: VideoDetails) {
                        viewModelScope.launch(Dispatchers.Main) {
                            itemDetailsFetched.send(CompletedItemMiniDetails(
                                    i,
                                    details.thumbnail,
                                    details.duration ?: "0s"
                            ))
                        }
                    }
                })
            }
        }
    }

    override fun observeItemDetailsFetched(lifecycleOwner: LifecycleOwner, observer: Observer<CompletedItemMiniDetails>) {
        itemDetailsFetched.observeSend(lifecycleOwner, observer)
    }

    override fun deleteItem(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = completedDao.load()
            if (index in 0 until items.count()) {
                val item = items[index]
                completedDao.delete(listOf(item))
            }
        }
    }

    override fun clearList() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = completedDao.load()
            completedDao.delete(items)
        }
    }
}