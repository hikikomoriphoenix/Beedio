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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksSQLite extends SQLiteOpenHelper {
    private String currentTable;

    public BookmarksSQLite(Context context) {
        super(context, "bookmarks.db", null, 1);
        currentTable = "bookmarks_root";
    }

    /*public static class BookmarkItem implements BaseColumns {
        public static final String TYPE = "type";
        public static final String ICON = "icon";
        public static final String CONTENTS = "contents";
        public static final String TITLE = "title";
        public static final String LINK = "link";
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks_root (position LONG, type TEXT, icon TEXT, contents" +
                " TEXT, title TEXT, link TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertNewEntryToEnd(String type, String icon, String contents,
                                    String title, String link) {
        long numRows = DatabaseUtils.queryNumEntries(getWritableDatabase(), currentTable);
        getWritableDatabase().execSQL(
                "INSERT into " + currentTable + " (position, type, icon, contents, title, link)" +
                        " VALUES(" + numRows + ", " + type + ", " + icon + ", " + contents + ", " + title +
                        ", " + link + ");");
    }

    public void setCurrentTable(String tableName) {
        currentTable = tableName;
    }
}
