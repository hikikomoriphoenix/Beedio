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

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import marabillas.loremar.lmvideodownloader.R;

public class AddBookmarkDialog extends Dialog {
    private Context context;
    private TextView destFolder;
    private List<String> folders;
    private Bookmark bookmark;
    private BookmarksSQLite bookmarksDB;
    private Cursor cursor;

    public AddBookmarkDialog(Context context, Bookmark bookmark) {
        super(context);
        this.context = context;
        this.bookmark = bookmark;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = View.inflate(context, R.layout.add_bookmark_dialog, null);
        setTitle("Add Bookmark");
        setContentView(view);

        bookmarksDB = new BookmarksSQLite(context);

        TextView title = view.findViewById(R.id.addBookmarkTitle);
        TextView url = view.findViewById(R.id.addBookmarkURL);
        destFolder = view.findViewById(R.id.addBookmarkDestFolder);
        RecyclerView folderList = view.findViewById(R.id.addBookmarkFoldersList);

        title.setText(bookmark.title);
        url.setText(bookmark.url);
        destFolder.setText(" ");
        cursor = bookmarksDB.getFolders();
        while (cursor.moveToNext()) {
            folders.add(cursor.getString(cursor.getColumnIndex("title")));
        }

        folderList.setAdapter(new FoldersAdapter());
        folderList.setLayoutManager(new LinearLayoutManager(context));
    }

    private class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FolderViewHolder> {
        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new FolderViewHolder(inflater.inflate(R.layout.add_bookmark_folders_list_item,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
            holder.bind(folders.get(position));
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class FolderViewHolder extends RecyclerView.ViewHolder {
            TextView nameView;

            FolderViewHolder(View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.addBookmarkFolderName);
            }

            void bind(String name) {
                nameView.setText(name);
            }
        }
    }
}
