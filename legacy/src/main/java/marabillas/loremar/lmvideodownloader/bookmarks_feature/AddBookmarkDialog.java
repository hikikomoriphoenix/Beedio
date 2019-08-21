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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class AddBookmarkDialog extends Dialog implements View.OnClickListener {
    private Activity activity;
    private TextView title;
    private TextView destFolder;
    private RecyclerView folderList;
    private List<String> folders;
    private Bookmark bookmark;
    private BookmarksSQLite bookmarksSQLite;
    private Cursor cursor;
    private TextView save;
    private TextView newFolder;
    private ImageView renameTitle;

    public AddBookmarkDialog(Activity activity, Bookmark bookmark) {
        super(activity);
        this.activity = activity;
        this.bookmark = bookmark;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = View.inflate(activity, R.layout.add_bookmark_dialog, null);
        setTitle("Add Bookmark");
        setContentView(view);
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        bookmarksSQLite = new BookmarksSQLite(activity);

        title = view.findViewById(R.id.addBookmarkTitle);
        TextView url = view.findViewById(R.id.addBookmarkURL);
        destFolder = view.findViewById(R.id.addBookmarkDestFolder);
        folderList = view.findViewById(R.id.addBookmarkFoldersList);
        save = view.findViewById(R.id.addBookmarkSave);
        newFolder = view.findViewById(R.id.addBookmarkNewFolder);
        renameTitle = view.findViewById(R.id.addBookmarkRenameTitle);

        title.setText(bookmark.title);
        url.setText(bookmark.url);
        destFolder.setText(activity.getResources().getString(R.string.bookmarks_root_folder));

        updateFolders();

        folderList.setAdapter(new FoldersAdapter());
        folderList.setLayoutManager(new LinearLayoutManager(activity));

        save.setOnClickListener(this);
        newFolder.setOnClickListener(this);
        renameTitle.setOnClickListener(this);
    }

    private void updateFolders() {
        cursor = bookmarksSQLite.getFolders();
        folders = new ArrayList<>();
        if (!bookmarksSQLite.getCurrentTable().equals(activity.getResources().getString(R.string
                .bookmarks_root_folder))) {
            folders.add("...");
        }
        while (cursor.moveToNext()) {
            folders.add(cursor.getString(cursor.getColumnIndex("title")));
        }
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        if (v == save) {
            byte[] bytes;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (bookmark.icon != null && bookmark.icon.compress(Bitmap.CompressFormat.PNG, 100,
                    out)) {
                bytes = out.toByteArray();
            } else {
                bytes = null;
            }
            bookmarksSQLite.add(bytes, bookmark.title, bookmark.url);
            dismiss();
            Toast.makeText(activity, "Page saved into bookmarks", Toast.LENGTH_SHORT).show();
        } else if (v == newFolder) {
            final EditText text = new EditText(activity);
            text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            new AlertDialog.Builder(activity)
                    .setMessage("Enter name of new folder.")
                    .setPositiveButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bookmarksSQLite.addFolder(text.getText().toString());
                            updateFolders();
                            folderList.getAdapter().notifyDataSetChanged();
                            Toast.makeText(activity, "New folder added", Toast.LENGTH_SHORT).show();
                            Utils.hideSoftKeyboard(activity, text.getWindowToken());
                        }
                    })
                    .setNegativeButton("CANCEL", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.hideSoftKeyboard(activity, text.getWindowToken());
                        }
                    })
                    .setView(text)
                    .create()
                    .show();
        } else if (v == renameTitle) {
            new RenameDialog(activity, bookmark.title) {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }

                @Override
                public void onOK(String newName) {
                    bookmark.title = newName;
                    title.setText(newName);
                }
            };
        }
    }

    private class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.FolderViewHolder> {
        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(activity);
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
                if (bookmarksSQLite.getCurrentTable().equals(activity.getResources().getString(R
                        .string.bookmarks_root_folder))) {
                    bookmarksSQLite.setCurrentTable(bookmarksSQLite.getCurrentTable() + "_" +
                            (getAdapterPosition() + 1));
                    destFolder.setText(nameView.getText());
                    updateFolders();
                    folderList.getAdapter().notifyDataSetChanged();
                } else {
                    if (getAdapterPosition() == 0) {
                        String upperTable = bookmarksSQLite.getCurrentTable()
                                .substring(0, bookmarksSQLite.getCurrentTable().lastIndexOf
                                        ("_"));
                        bookmarksSQLite.setCurrentTable(upperTable);
                        updateFolders();
                        folderList.getAdapter().notifyDataSetChanged();
                        if (upperTable.equals(activity.getResources().getString(R.string
                                .bookmarks_root_folder))) {
                            destFolder.setText(upperTable);
                        } else {
                            String positionInParentTable = upperTable.substring(upperTable.lastIndexOf
                                    ("_") + 1, upperTable.length());
                            String parentTable = upperTable.substring(0, upperTable.lastIndexOf("_"));
                            Cursor c = bookmarksSQLite.getBookmarksDatabase().query(parentTable,
                                    new String[]{"title"}, "oid = " + positionInParentTable,
                                    null, null, null, null);
                            c.moveToNext();
                            String dest = c.getString(c.getColumnIndex("title"));
                            c.close();
                            destFolder.setText(dest);
                        }
                    } else {
                        bookmarksSQLite.setCurrentTable(bookmarksSQLite.getCurrentTable() + "_" +
                                getAdapterPosition());
                        destFolder.setText(nameView.getText());
                        updateFolders();
                        folderList.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
