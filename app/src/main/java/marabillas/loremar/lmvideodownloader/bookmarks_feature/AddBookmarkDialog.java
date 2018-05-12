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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.R;

public class AddBookmarkDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView destFolder;
    private List<String> folders;
    private Bookmark bookmark;
    private BookmarksSQLite bookmarksSQLite;
    private Cursor cursor;
    private TextView save;

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

        bookmarksSQLite = new BookmarksSQLite(context);

        TextView title = view.findViewById(R.id.addBookmarkTitle);
        TextView url = view.findViewById(R.id.addBookmarkURL);
        destFolder = view.findViewById(R.id.addBookmarkDestFolder);
        RecyclerView folderList = view.findViewById(R.id.addBookmarkFoldersList);
        save = view.findViewById(R.id.addBookmarkSave);

        title.setText(bookmark.title);
        url.setText(bookmark.url);
        destFolder.setText(context.getResources().getString(R.string.bookmarks_root_folder));
        cursor = bookmarksSQLite.getFolders();
        folders = new ArrayList<>();
        while (cursor.moveToNext()) {
            folders.add(cursor.getString(cursor.getColumnIndex("title")));
        }
        cursor.close();

        folderList.setAdapter(new FoldersAdapter());
        folderList.setLayoutManager(new LinearLayoutManager(context));

        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == save) {
            byte[] bytes;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (bookmark.icon != null && bookmark.icon.compress(Bitmap.CompressFormat.PNG, 100,
                    out)) {
                bytes = out.toByteArray();
                Log.i("loremarTest", "an icon is saved");
            } else {
                bytes = null;
                Log.i("loremarTest", "icon is null");
            }
            bookmarksSQLite.add(bytes, bookmark.title, bookmark.url);
            dismiss();
            Log.i("loremarTest", "a bookmark is saved");
        }
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
            return folders.size();
        }

        class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView nameView;

            FolderViewHolder(View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.addBookmarkFolderName);
                itemView.setOnClickListener(this);
            }

            void bind(String name) {
                nameView.setText(name);
            }

            @Override
            public void onClick(View v) {

            }
        }
    }
}
