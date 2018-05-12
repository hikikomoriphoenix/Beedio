/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.lmvideodownloader.bookmarks_feature;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import marabillas.loremar.lmvideodownloader.R;

public class BookmarksSQLite extends SQLiteOpenHelper {
    private String currentTable;
    private SQLiteDatabase bookmarksDB;

    public BookmarksSQLite(Context context) {
        super(context, "bookmarks.db", null, 1);
        currentTable = context.getResources().getString(R.string.bookmarks_root_folder);
        bookmarksDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks (type TEXT, icon BLOB, title TEXT, link TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(byte[] icon, String title, String link) {
        ContentValues v = new ContentValues();
        v.put("type", "link");
        v.put("icon", icon);
        v.put("title", title);
        v.put("link", link);
        bookmarksDB.insert(currentTable, null, v);
    }

    public void insert(int position, String type, byte[] icon, String title, String link) {
        for (int i = (int) DatabaseUtils.queryNumEntries(bookmarksDB, currentTable); i
                >= position; i--) {
            bookmarksDB.execSQL("UPDATE " + currentTable + " SET " + "oid = oid + 1 " +
                    "WHERE oid = " + i);
        }
        if (type.equals("folder")) {
            bookmarksDB.execSQL("INSERT INTO " + currentTable + " (oid, type, title) VALUES (" +
                    position + ", '" + type + "', '" + title + "')");
            bookmarksDB.execSQL("CREATE TABLE " + currentTable + "_" + position + " (type " +
                    "TEXT, icon TEXT, title TEXT, link TEXT);");
        } else {
            ContentValues v = new ContentValues();
            v.put("oid", position);
            v.put("type", type);
            v.put("icon", icon);
            v.put("title", title);
            v.put("link", link);
            bookmarksDB.insert(currentTable, null, v);
        }
    }

    public void delete(int position) {
        delete(currentTable, position);
    }

    private void delete(String table, int position) {
        if (getType(position).equals("folder")) {
            deleteFolder(table + "_" + position);
        }
        bookmarksDB.execSQL("DELETE FROM " + table + " WHERE oid = " + position);
        bookmarksDB.execSQL("VACUUM");
    }

    private void deleteFolder(String table) {
        Cursor c = getFolders(table);
        while (c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("oid"));
            deleteFolder(table + "_" + index);
        }
        bookmarksDB.execSQL("DROP TABLE " + table);
        c.close();
    }

    public void moveItem(String sourceTable, int sourcePosition, int destPosition) {
        Cursor c = bookmarksDB.query(sourceTable, null, "oid = " + sourcePosition, null,
                null, null, null);
        c.moveToNext();
        insert(destPosition, c.getString(c.getColumnIndex("type")), c.getBlob(c.getColumnIndex
                ("icon")), c.getString(c.getColumnIndex("title")), c.getString(c.getColumnIndex
                ("link")));
        delete(sourceTable, sourcePosition);
        c.close();
    }

    private String getType(int position) {
        Cursor c = bookmarksDB.query(currentTable, new String[]{"type"}, "oid = " +
                position, null, null, null, null);
        c.moveToNext();
        String type = c.getString(c.getColumnIndex("type"));
        c.close();
        return type;
    }

    public void setCurrentTable(String tableName) {
        currentTable = tableName;
    }

    public String getCurrentTable() {
        return currentTable;
    }

    public Cursor getBookmarks() {
        return bookmarksDB.query(currentTable, null, null, null, null, null, null);
    }

    public Cursor getFolders(String table) {
        return bookmarksDB.query(table, new String[]{"oid"}, "type = " +
                "'folder'", null, null, null, null);
    }

    public Cursor getFolders() {
        return bookmarksDB.query(currentTable, new String[]{"oid"}, "type = " +
                "'folder'", null, null, null, null);
    }

    public void addFolder(String name) {
        Cursor c = getFolders();
        insert(c.getCount(), "folder", null, name, null);
        c.close();
    }
}
