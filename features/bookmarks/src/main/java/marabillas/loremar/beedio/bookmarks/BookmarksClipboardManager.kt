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

package marabillas.loremar.beedio.bookmarks

import android.database.Cursor
import android.database.DatabaseUtils
import marabillas.loremar.beedio.base.database.BookmarksSQLite

internal class BookmarksClipboardManager(private val sqLite: BookmarksSQLite) : BookmarksSQLite.OnBookmarkPositionChangedListener {
    var clipBoard: ClipBoard? = null
        private set

    internal inner class ClipBoard {
        var table: String = BookmarksSQLite.ROOT_FOLDER
        var position = 0
        var type: String = "folder"
        var icon: ByteArray? = null
        var title: String = ""
        var link: String? = null
        var move = false
    }

    override fun onBookmarkPositionChanged(oldPosition: Int, newPosition: Int) {
        if (clipBoard != null && clipBoard!!.position == oldPosition) {
            if (newPosition > 0) {
                clipBoard!!.position = newPosition
            } else {
                clipBoard = null
            }
        }
    }

    private fun storeToClipboard(position: Int) {
        val c: Cursor = sqLite.bookmarksDatabase.query(sqLite.currentTable, null, "oid = " +
                position, null, null, null, null)
        c.moveToNext()
        clipBoard = ClipBoard()
        clipBoard!!.table = sqLite.currentTable
        clipBoard!!.position = position
        clipBoard!!.type = c.getString(c.getColumnIndex("type"))
        clipBoard!!.icon = c.getBlob(c.getColumnIndex("icon"))
        clipBoard!!.title = c.getString(c.getColumnIndex("title"))
        clipBoard!!.link = c.getString(c.getColumnIndex("link"))
        c.close()
    }

    fun copy(position: Int) {
        storeToClipboard(position)
        clipBoard!!.move = false
    }

    fun cut(position: Int) {
        storeToClipboard(position)
        clipBoard!!.move = true
    }

    fun paste(): Boolean {
        return if (clipBoard!!.type == "link") {
            paste(DatabaseUtils.queryNumEntries(sqLite.bookmarksDatabase, sqLite
                    .currentTable).toInt() + 1)
        } else {
            val c: Cursor = sqLite.folders
            val pasted = paste(c.count + 1)
            c.close()
            pasted
        }
    }

    fun paste(position: Int): Boolean {
        return clipBoard?.let { it ->
            if (!it.move) {
                sqLite.insert(position, it.type, it.icon, it.title, it.link)
                if (it.type == "folder") {
                    sqLite.copyFolderContents(it.table + "_" + it.position, sqLite
                            .currentTable + "_" + position)
                }
                true
            } else {
                sqLite.moveItem(it.table, it.position, position)
                clipBoard = null
                true
            }
        } ?: false
    }

    val isClipboardEmpty: Boolean
        get() = clipBoard == null

    init {
        this.sqLite.setOnBookmarkPositionChangedListener(this)
    }
}