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

package marabillas.loremar.beedio.base.database

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.threeten.bp.ZonedDateTime

@Dao
interface HistoryDao {
    @Query("SELECT * FROM historyitem ORDER BY datetime(date) DESC")
    fun getAll(): List<HistoryItem>

    @Query("SELECT COUNT(url) FROM historyitem WHERE url = :url")
    fun countItemsWithUrl(url: String): Int

    @Insert
    fun add(item: HistoryItem)

    @Delete
    fun delete(item: HistoryItem)

    @Delete
    fun delete(items: List<HistoryItem>)

    @Query("UPDATE historyitem SET title = :title, date = :date, favicon = :favicon WHERE url = :url")
    fun updateItem(title: String, date: ZonedDateTime, favicon: Bitmap?, url: String)

    @Query("UPDATE historyitem SET favicon = :icon WHERE url=:url")
    fun updateNewIcon(icon: Bitmap, url: String)
}