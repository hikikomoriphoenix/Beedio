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

import android.database.Cursor;

class BookmarksClipboardManager {
    private ClipBoard clipBoard;
    private BookmarksSQLite sqLite;

    class ClipBoard {
        String table;
        int position;
        String type;
        byte[] icon;
        String title;
        String link;
        boolean move;
    }

    BookmarksClipboardManager(BookmarksSQLite sqLite) {
        this.sqLite = sqLite;
    }

    private void storeToClipboard(int position) {
        Cursor c = sqLite.getBookmarksDatabase().query(sqLite.getCurrentTable(), null, "oid = " +
                position, null, null, null, null);
        c.moveToNext();
        clipBoard = new ClipBoard();
        clipBoard.table = sqLite.getCurrentTable();
        clipBoard.position = position;
        clipBoard.type = c.getString(c.getColumnIndex("type"));
        clipBoard.icon = c.getBlob(c.getColumnIndex("icon"));
        clipBoard.title = c.getString(c.getColumnIndex("title"));
        clipBoard.link = c.getString(c.getColumnIndex("link"));
        c.close();
    }

    void copy(int position) {
        storeToClipboard(position);
        clipBoard.move = false;
    }

    void cut(int position) {
        storeToClipboard(position);
        clipBoard.move = true;
    }

    boolean paste(int position) {
        if (!clipBoard.move) {
            sqLite.insert(position, clipBoard.type, clipBoard.icon, clipBoard.title, clipBoard.link);
            return true;
        } else {
            //todo fix: folder dont have links
            Cursor c = sqLite.getBookmarksDatabase().query(clipBoard.table, new String[]{"link"},
                    "oid = " + clipBoard.position, null, null, null, null, null);
            c.moveToNext();
            if (clipBoard.link.equals(c.getString(c.getColumnIndex("link")))) {
                sqLite.moveItem(clipBoard.table, clipBoard.position, position);
                c.close();
                clipBoard = null;
                return true;
            } else {
                c.close();
                clipBoard = null;
                return false;
            }
        }
    }

    boolean isClipboardEmpty() {
        return clipBoard == null;
    }

    ClipBoard getClipBoard() {
        return clipBoard;
    }
}
