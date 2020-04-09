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

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import javax.inject.Inject

class BookmarksSQLite @Inject constructor(context: Context) : SQLiteOpenHelper(context, "bookmarks.db", null, 1) {
    var currentTable: String
    val bookmarksDatabase: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE bookmarks (type TEXT, icon BLOB, title TEXT, link TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(icon: ByteArray?, title: String?, link: String?) {
        add(currentTable, icon, title, link)
    }

    fun add(table: String?, icon: ByteArray?, title: String?, link: String?) {
        val v = ContentValues()
        v.put("type", "link")
        v.put("icon", icon)
        v.put("title", title)
        v.put("link", link)
        bookmarksDatabase.insert(table, null, v)
    }

    fun insert(position: Int, type: String, icon: ByteArray?, title: String, link: String?) {
        insert(currentTable, position, type, icon, title, link)
    }

    fun insert(table: String, position: Int, type: String, icon: ByteArray?, title: String, link: String?) {
        var i = DatabaseUtils.queryNumEntries(bookmarksDatabase, table).toInt()
        while (i
                >= position) {
            val c = bookmarksDatabase.query(table, arrayOf("type"), "oid = $i", null,
                    null, null, null)
            if (c.moveToNext() && c.getString(c.getColumnIndex("type")) == "folder") {
                val tablename = table + "_" + i
                val newTablename = table + "_" + (i + 1)
                bookmarksDatabase.execSQL("ALTER TABLE $tablename RENAME TO $newTablename")
                renameSubFolderTables(tablename, newTablename)
            }
            c.close()
            bookmarksDatabase.execSQL("UPDATE " + table + " SET " + "oid = oid + 1 " +
                    "WHERE oid = " + i)
            if (onBookmarkPositionChangedListener != null) {
                onBookmarkPositionChangedListener!!.onBookmarkPositionChanged(i, i + 1)
            }
            i--
        }
        if (type == "folder") {
            bookmarksDatabase.execSQL("INSERT INTO " + table + " (oid, type, title) VALUES (" +
                    position + ", '" + type + "', '" + title + "')")
            bookmarksDatabase.execSQL("CREATE TABLE " + table + "_" + position + " (type " +
                    "TEXT, icon BLOB, title TEXT, link TEXT);")
        } else {
            val v = ContentValues()
            v.put("oid", position)
            v.put("type", type)
            v.put("icon", icon)
            v.put("title", title)
            v.put("link", link)
            bookmarksDatabase.insert(table, null, v)
        }
    }

    fun delete(position: Int) {
        delete(currentTable, position)
    }

    private fun delete(table: String, position: Int) {
        if (getType(table, position) == "folder") {
            deleteFolderContents(table + "_" + position)
        }
        for (i in position + 1..DatabaseUtils.queryNumEntries(bookmarksDatabase, table)) {
            val c = bookmarksDatabase.query(table, arrayOf("type"), "oid = $i", null,
                    null, null, null)
            if (c.moveToNext() && c.getString(c.getColumnIndex("type")) == "folder") {
                val tablename = table + "_" + i
                val newTablename = table + "_" + (i - 1)
                bookmarksDatabase.execSQL("ALTER TABLE $tablename RENAME TO $newTablename")
                renameSubFolderTables(tablename, newTablename)
                if (tablename == currentTable) {
                    currentTable = newTablename
                }
            }
            c.close()
            onBookmarkPositionChangedListener!!.onBookmarkPositionChanged(i.toInt(), (i - 1).toInt())
        }
        bookmarksDatabase.execSQL("DELETE FROM $table WHERE oid = $position")
        bookmarksDatabase.execSQL("VACUUM")
        onBookmarkPositionChangedListener!!.onBookmarkPositionChanged(position, -1)
    }

    private fun deleteFolderContents(table: String) {
        val c = getFolders(table)
        while (c.moveToNext()) {
            val index = c.getInt(0) //apparently getColumnIndex("oid") returns -1(!?)
            deleteFolderContents(table + "_" + index)
        }
        bookmarksDatabase.execSQL("DROP TABLE $table")
        c.close()
    }

    private fun renameSubFolderTables(oldBasename: String, newBasename: String) {
        val c = getFolders(newBasename)
        while (c.moveToNext()) {
            val position = c.getInt(0)
            val oldTablename = oldBasename + "_" + position
            val newTablename = newBasename + "_" + position
            bookmarksDatabase.execSQL("ALTER TABLE $oldTablename RENAME TO $newTablename")
            renameSubFolderTables(oldTablename, newTablename)
            if (oldTablename == currentTable) {
                currentTable = newTablename
            }
        }
    }

    fun moveItem(sourceTable: String, sourcePosition: Int, destPosition: Int) {
        val c = bookmarksDatabase.query(sourceTable, null, "oid = $sourcePosition", null,
                null, null, null)
        c.moveToNext()
        insert(destPosition, c.getString(c.getColumnIndex("type")), c.getBlob(c.getColumnIndex("icon")), c.getString(c.getColumnIndex("title")), c.getString(c.getColumnIndex("link")))
        if (sourceTable == currentTable && sourcePosition >= destPosition) {
            if (c.getString(c.getColumnIndex("type")) == "folder") {
                copyFolderContents(sourceTable + "_" + sourcePosition, currentTable + "_" + destPosition)
            }
            delete(sourceTable, sourcePosition + 1)
        } else {
            if (c.getString(c.getColumnIndex("type")) == "folder") {
                copyFolderContents(sourceTable + "_" + sourcePosition, currentTable + "_" + destPosition)
            }
            delete(sourceTable, sourcePosition)
        }
        c.close()
    }

    fun copyFolderContents(sourceTable: String, destTable: String) {
        val source = bookmarksDatabase.query(sourceTable, arrayOf("oid", "type", "icon",
                "title", "link"), null, null, null, null, null)
        while (source.moveToNext()) {
            if (source.getString(source.getColumnIndex("type")) == "folder") {
                val dest = getFolders(destTable)
                val destPosition = dest.count + 1
                insert(destTable, destPosition, "folder", null, source.getString(source
                        .getColumnIndex("title")), null)
                dest.close()
                val sourcePosition = source.getInt(0)
                copyFolderContents(sourceTable + "_" + sourcePosition, destTable + "_" + destPosition)
            } else {
                add(destTable, source.getBlob(source.getColumnIndex("icon")), source.getString(source.getColumnIndex("title")), source.getString(source.getColumnIndex("link")))
            }
        }
        source.close()
    }

    private var onBookmarkPositionChangedListener: OnBookmarkPositionChangedListener? = null

    interface OnBookmarkPositionChangedListener {
        fun onBookmarkPositionChanged(oldPosition: Int, newPosition: Int)
    }

    fun setOnBookmarkPositionChangedListener(onBookmarkPositionChangedListener: OnBookmarkPositionChangedListener?) {
        this.onBookmarkPositionChangedListener = onBookmarkPositionChangedListener
    }

    private fun getType(table: String, position: Int): String {
        val c = bookmarksDatabase.query(table, arrayOf("type"), "oid = " +
                position, null, null, null, null)
        c.moveToNext()
        val type = c.getString(c.getColumnIndex("type"))
        c.close()
        return type
    }

    val bookmarks: Cursor
        get() = bookmarksDatabase.query(currentTable, null, null, null, null, null, null)

    fun getFolders(table: String?): Cursor {
        return bookmarksDatabase.query(table, arrayOf("oid", "title"), "type = " +
                "'folder'", null, null, null, null)
    }

    val folders: Cursor
        get() = bookmarksDatabase.query(currentTable, arrayOf("oid", "title"), "type = " +
                "'folder'", null, null, null, null)

    fun addFolder(name: String) {
        val c = folders
        insert(c.count + 1, "folder", null, name, null)
        c.close()
    }

    fun renameBookmarkTitle(position: Int, newTitle: String) {
        bookmarksDatabase.execSQL("UPDATE " + currentTable + " SET title = '" + newTitle + "' WHERE oid" +
                " = " + position)
    }

    init {
        currentTable = ROOT_FOLDER
        bookmarksDatabase = writableDatabase
    }

    companion object {
        const val ROOT_FOLDER = "bookmarks"
    }
}