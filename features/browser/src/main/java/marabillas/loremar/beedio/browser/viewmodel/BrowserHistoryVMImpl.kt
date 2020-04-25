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

package marabillas.loremar.beedio.browser.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import marabillas.loremar.beedio.base.database.HistoryDatabase
import marabillas.loremar.beedio.base.database.HistoryItem
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class BrowserHistoryVMImpl(context: Context) : BrowserHistoryVM() {
    private val historyDao = Room.databaseBuilder(
            context,
            HistoryDatabase::class.java,
            "history").build()
            .historyDao()

    override fun addNewVisitedPage(url: String, title: String, icon: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {
            val isStored = historyDao.countItemsWithUrl(url) > 0
            if (isStored) {
                historyDao.updateItem(title,
                        ZonedDateTime.now(ZoneId.systemDefault()),
                        icon,
                        url)
            } else {
                historyDao.add(HistoryItem(0,
                        url = url,
                        title = title,
                        date = ZonedDateTime.now(ZoneId.systemDefault()),
                        favicon = icon))
            }
        }
    }

    override fun updateVisitedPageIcon(url: String, icon: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.updateNewIcon(icon, url)
        }
    }
}