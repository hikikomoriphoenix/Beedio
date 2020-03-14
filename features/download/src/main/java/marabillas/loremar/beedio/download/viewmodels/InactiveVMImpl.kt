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

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import marabillas.loremar.beedio.base.database.DownloadListDatabase
import marabillas.loremar.beedio.base.download.VideoDownloader
import java.io.File
import kotlin.math.roundToInt

class InactiveVMImpl(private val context: Context, downloadDB: DownloadListDatabase) : InactiveVM() {
    private val inactiveDao = downloadDB.inactiveListDao()

    override fun loadList(actionOnComplete: (List<InactiveItem>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val inactiveList = inactiveDao.load().toInactiveList()
            viewModelScope.launch(Dispatchers.Main) {
                actionOnComplete(inactiveList)
            }
        }
    }

    private fun List<marabillas.loremar.beedio.base.database.InactiveItem>.toInactiveList()
            : List<InactiveItem> {

        val list = mutableListOf<InactiveItem>()
        forEach {
            val item = InactiveItem(
                    filename = "${it.name}.${it.ext}",
                    downloaded = "${it.getProgress()} ${it.getDownloadedText()}",
                    sourceWebpage = it.sourceWebpage,
                    size = it.size
            )
            list.add(item)
        }
        return list
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getDownloadedText(): String {
        return if (size == 0L)
            getDownloaded().formatSize()
        else
            "${getDownloaded().formatSize()} / ${size.formatSize()}"
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getProgress(): Int? {
        return if (size > 0L) {
            val percent = (getDownloaded().toDouble() / size.toDouble()) * 100.0
            if (percent > 100)
                100
            else
                percent.roundToInt()
        } else
            null
    }

    private fun marabillas.loremar.beedio.base.database.InactiveItem.getDownloaded(): Long {
        val filename = "$name.$ext"
        val file = File(VideoDownloader.getDownloadFolder(context), filename)
        return file.length()
    }

    private fun Long.formatSize(): String = Formatter.formatFileSize(context, this)

    override fun deleteItem(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = inactiveDao.load()[index]
            inactiveDao.delete(listOf(item))
        }
    }

    override fun clearList() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = inactiveDao.load()
            inactiveDao.delete(list)
        }
    }
}