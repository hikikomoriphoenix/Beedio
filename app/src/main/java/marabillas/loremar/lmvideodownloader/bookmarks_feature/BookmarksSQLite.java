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
        add(currentTable, icon, title, link);
    }

    public void add(String table, byte[] icon, String title, String link) {
        ContentValues v = new ContentValues();
        v.put("type", "link");
        v.put("icon", icon);
        v.put("title", title);
        v.put("link", link);
        bookmarksDB.insert(table, null, v);
    }

    public void insert(int position, String type, byte[] icon, String title, String link) {
        insert(currentTable, position, type, icon, title, link);
    }

    public void insert(String table, int position, String type, byte[] icon, String title, String
            link) {
        for (int i = (int) DatabaseUtils.queryNumEntries(bookmarksDB, table); i
                >= position; i--) {
            Cursor c = bookmarksDB.query(table, new String[]{"type"}, "oid = " + i, null,
                    null, null, null);
            if (c.moveToNext() && c.getString(c.getColumnIndex("type")).equals("folder")) {
                String tablename = table + "_" + i;
                String newTablename = table + "_" + (i + 1);
                bookmarksDB.execSQL("ALTER TABLE " + tablename + " RENAME TO " + newTablename);
                renameSubFolderTables(tablename, newTablename);
            }
            c.close();

            bookmarksDB.execSQL("UPDATE " + table + " SET " + "oid = oid + 1 " +
                    "WHERE oid = " + i);

            if (onBookmarkPositionChangedListener != null) {
                onBookmarkPositionChangedListener.onBookmarkPositionChanged(i, i + 1);
            }
        }
        if (type.equals("folder")) {
            bookmarksDB.execSQL("INSERT INTO " + table + " (oid, type, title) VALUES (" +
                    position + ", '" + type + "', '" + title + "')");
            bookmarksDB.execSQL("CREATE TABLE " + table + "_" + position + " (type " +
                    "TEXT, icon BLOB, title TEXT, link TEXT);");
        } else {
            ContentValues v = new ContentValues();
            v.put("oid", position);
            v.put("type", type);
            v.put("icon", icon);
            v.put("title", title);
            v.put("link", link);
            bookmarksDB.insert(table, null, v);
        }
    }

    public void delete(int position) {
        delete(currentTable, position);
    }

    private void delete(String table, int position) {
        if (getType(table, position).equals("folder")) {
            deleteFolderContents(table + "_" + position);
        }

        for (int i = position + 1; i <= DatabaseUtils.queryNumEntries(bookmarksDB, table); i++) {
            Cursor c = bookmarksDB.query(table, new String[]{"type"}, "oid = " + i, null,
                    null, null, null);
            if (c.moveToNext() && c.getString(c.getColumnIndex("type")).equals("folder")) {
                String tablename = table + "_" + i;
                String newTablename = table + "_" + (i - 1);
                bookmarksDB.execSQL("ALTER TABLE " + tablename + " RENAME TO " + newTablename);
                renameSubFolderTables(tablename, newTablename);
                if (tablename.equals(currentTable)) {
                    currentTable = newTablename;
                }
            }
            c.close();
            onBookmarkPositionChangedListener.onBookmarkPositionChanged(i, i - 1);
        }

        bookmarksDB.execSQL("DELETE FROM " + table + " WHERE oid = " + position);
        bookmarksDB.execSQL("VACUUM");
        onBookmarkPositionChangedListener.onBookmarkPositionChanged(position, -1);
    }

    private void deleteFolderContents(String table) {
        Cursor c = getFolders(table);
        while (c.moveToNext()) {
            int index = c.getInt(0);//apparently getColumnIndex("oid") returns -1(!?)
            deleteFolderContents(table + "_" + index);
        }
        bookmarksDB.execSQL("DROP TABLE " + table);
        c.close();
    }

    private void renameSubFolderTables(String oldBasename, String newBasename) {
        Cursor c = getFolders(newBasename);
        while (c.moveToNext()) {
            int position = c.getInt(0);
            String oldTablename = oldBasename + "_" + position;
            String newTablename = newBasename + "_" + position;
            bookmarksDB.execSQL("ALTER TABLE " + oldTablename + " RENAME TO " + newTablename);
            renameSubFolderTables(oldTablename, newTablename);
            if (oldTablename.equals(currentTable)) {
                currentTable = newTablename;
            }
        }
    }

    public void moveItem(String sourceTable, int sourcePosition, int destPosition) {
        Cursor c = bookmarksDB.query(sourceTable, null, "oid = " + sourcePosition, null,
                null, null, null);
        c.moveToNext();
        insert(destPosition, c.getString(c.getColumnIndex("type")), c.getBlob(c.getColumnIndex
                ("icon")), c.getString(c.getColumnIndex("title")), c.getString(c.getColumnIndex
                ("link")));
        if (sourceTable.equals(currentTable) && sourcePosition >= destPosition) {
            if (c.getString(c.getColumnIndex("type")).equals("folder")) {
                copyFolderContents(sourceTable + "_" + sourcePosition, currentTable + "_" + destPosition);
            }
            delete(sourceTable, sourcePosition + 1);
        } else {
            if (c.getString(c.getColumnIndex("type")).equals("folder")) {
                copyFolderContents(sourceTable + "_" + sourcePosition, currentTable + "_" + destPosition);
            }
            delete(sourceTable, sourcePosition);
        }
        c.close();
    }

    public void copyFolderContents(String sourceTable, String destTable) {
        Cursor source = bookmarksDB.query(sourceTable, new String[]{"oid", "type", "icon",
                "title", "link"}, null, null, null, null, null);
        while (source.moveToNext()) {
            if (source.getString(source.getColumnIndex("type")).equals("folder")) {
                Cursor dest = getFolders(destTable);
                int destPosition = dest.getCount() + 1;
                insert(destTable, destPosition, "folder", null, source.getString(source
                        .getColumnIndex("title")), null);
                dest.close();
                int sourcePosition = source.getInt(0);
                copyFolderContents(sourceTable + "_" + sourcePosition, destTable + "_" + destPosition);
            } else {
                add(destTable, source.getBlob(source.getColumnIndex("icon")), source.getString
                        (source.getColumnIndex("title")), source.getString(source.getColumnIndex
                        ("link")));
            }
        }
        source.close();
    }

    private OnBookmarkPositionChangedListener onBookmarkPositionChangedListener;

    public interface OnBookmarkPositionChangedListener {
        void onBookmarkPositionChanged(int oldPosition, int newPosition);
    }

    public void setOnBookmarkPositionChangedListener(OnBookmarkPositionChangedListener
                                                             onBookmarkPositionChangedListener) {
        this.onBookmarkPositionChangedListener = onBookmarkPositionChangedListener;
    }

    private String getType(String table, int position) {
        Cursor c = bookmarksDB.query(table, new String[]{"type"}, "oid = " +
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
        return bookmarksDB.query(table, new String[]{"oid", "title"}, "type = " +
                "'folder'", null, null, null, null);
    }

    public Cursor getFolders() {
        return bookmarksDB.query(currentTable, new String[]{"oid", "title"}, "type = " +
                "'folder'", null, null, null, null);
    }

    public void addFolder(String name) {
        Cursor c = getFolders();
        insert(c.getCount() + 1, "folder", null, name, null);
        c.close();
    }

    public SQLiteDatabase getBookmarksDatabase() {
        return bookmarksDB;
    }

    public void renameBookmarkTitle(int position, String newTitle) {
        bookmarksDB.execSQL("UPDATE " + currentTable + " SET title = '" + newTitle + "' WHERE oid" +
                " = " + position);
    }
}
