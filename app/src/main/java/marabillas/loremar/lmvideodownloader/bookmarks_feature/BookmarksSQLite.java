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

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksSQLite extends SQLiteOpenHelper {
    private String currentTable;
    private SQLiteDatabase bookmarksDB;

    public BookmarksSQLite(Context context) {
        super(context, "bookmarks.db", null, 1);
        currentTable = "bookmarks";
        bookmarksDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks (type TEXT, icon TEXT, title TEXT, link TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(String type, String icon, String title, String link) {
        bookmarksDB.execSQL(
                "INSERT into " + currentTable + " VALUES ('" + type + "', '" + icon + "', " +
                        title + "', '" + link + "')");
        if (type.equals("folder")) {
            long numEntries = DatabaseUtils.queryNumEntries(bookmarksDB, currentTable);
            bookmarksDB.execSQL("CREATE TABLE " + currentTable + "_" + (numEntries + 1) + "(type " +
                    "TEXT, icon TEXT, title TEXT, link TEXT)");
        }
    }

    public void insert(int position, String type, String icon, String title, String link) {
        if (getType(position).equals("link")) {
            for (int i = (int) DatabaseUtils.queryNumEntries(bookmarksDB, currentTable); i
                    >= position; i--) {
                bookmarksDB.execSQL("UPDATE " + currentTable + " SET " + "oid = oid + 1 " +
                        "WHERE oid = " + i);
            }
            bookmarksDB.execSQL("INSERT INTO " + currentTable + "(oid, type, icon, title, link)" +
                    " VALUES (" + position + ", '" + type + "', '" + icon + "', '" + title + "', '" +
                    link + "')");
            if (type.equals("folder")) {
                bookmarksDB.execSQL("CREATE TABLE " + currentTable + "_" + position + " (type " +
                        "TEXT, icon TEXT, title TEXT, link TEXT);");
            }
        } else {
            String folderContents = currentTable + "_" + position;
            bookmarksDB.execSQL("INSERT INTO " + folderContents + " VALUES (type, icon," +
                    " title, link)");
            if (type.equals("folder")) {
                long numEntries = DatabaseUtils.queryNumEntries(bookmarksDB, folderContents);
                bookmarksDB.execSQL("CREATE TABLE " + folderContents + "_" + (numEntries + 1) +
                        " (type TEXT, icon TEXT, title TEXT, link TEXT);");
            }
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
        Cursor c = bookmarksDB.query(table, new String[]{"oid"}, "type = " +
                "folder", null, null, null, null);
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
        insert(destPosition, c.getString(c.getColumnIndex("type")), c.getString(c.getColumnIndex
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

    public Cursor getBookmarks() {
        return bookmarksDB.query(currentTable, null, null, null, null, null, null);
    }
}
