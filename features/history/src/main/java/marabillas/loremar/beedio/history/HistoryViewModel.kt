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

package marabillas.loremar.beedio.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import marabillas.loremar.beedio.base.database.HistoryDatabase
import marabillas.loremar.beedio.base.database.HistoryItem

class HistoryViewModel(context: Context) : ViewModel() {
    private val historyDao = Room.databaseBuilder(context,
            HistoryDatabase::class.java,
            "history")
            .build()
            .historyDao()

    fun loadAllItems(callback: (List<HistoryItem>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = historyDao.getAll()
            viewModelScope.launch(Dispatchers.Main) {
                callback(list)
            }
        }
    }

    fun deleteItem(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.delete(item)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.delete(historyDao.getAll())
        }
    }
}