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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.utils.RenameDialog;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class Bookmarks extends LMvdFragment implements LMvdActivity.OnBackPressedListener {
    private View view;
    private RecyclerView bookmarksView;
    private List<BookmarksItem> bookmarks;
    private BookmarksSQLite bookmarksSQLite;
    private BookmarksClipboardManager bookmarksClipboardManager;
    private TextView pasteButton;

    @Override
    public void onBackpressed() {
        getLMvdActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    class BookmarksItem {
        String type;
        Drawable icon;
        String title;
        String url;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.bookmarks, container, false);
            bookmarksView = view.findViewById(R.id.bookmarks);
            bookmarksSQLite = new BookmarksSQLite(getActivity());
            bookmarksClipboardManager = new BookmarksClipboardManager(bookmarksSQLite);

            view.findViewById(R.id.bookmarksMenuButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DrawerLayout) getActivity().findViewById(R.id.drawer)).openDrawer(GravityCompat.START);
                }
            });
            getLMvdActivity().setOnBackPressedListener(this);

            loadBookmarksData();

            bookmarksView.setAdapter(new BookmarksAdapter());
            bookmarksView.setLayoutManager(new LinearLayoutManager(getActivity()));
            bookmarksView.setHasFixedSize(true);
            bookmarksView.addItemDecoration(Utils.createDivider(getActivity()));

            view.findViewById(R.id.bookmarksNewFolder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText text = new EditText(getActivity());
                    text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Enter name of new folder.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    bookmarksSQLite.addFolder(text.getText().toString());
                                    loadBookmarksData();
                                    bookmarksView.getAdapter().notifyDataSetChanged();
                                    Toast.makeText(getActivity(), "New folder added", Toast.LENGTH_SHORT).show();
                                    Utils.hideSoftKeyboard(getActivity(), text.getWindowToken());
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utils.hideSoftKeyboard(getActivity(), text.getWindowToken());
                                }
                            })
                            .setView(text)
                            .create()
                            .show();
                }
            });

            pasteButton = view.findViewById(R.id.bookmarksPaste);
            pasteButton.setOnClickListener(new View
                    .OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean pasted;
                    pasted = bookmarksClipboardManager.paste();
                    if (!pasted) {
                        Toast.makeText(getActivity(), "Bookmark to move no " +
                                "longer exist", Toast.LENGTH_SHORT).show();
                    } else {
                        loadBookmarksData();
                        bookmarksView.getAdapter().notifyDataSetChanged();
                    }
                    if (bookmarksClipboardManager.isClipboardEmpty()) {
                        pasteButton.setVisibility(View.GONE);
                    }
                }
            });
            pasteButton.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadBookmarksData() {
        bookmarks = new ArrayList<>();
        if (!bookmarksSQLite.getCurrentTable().equals(getResources().getString(R.string
                .bookmarks_root_folder))) {
            BookmarksItem b = new BookmarksItem();
            b.type = "upFolder";
            b.icon = getResources().getDrawable(R.drawable.ic_folder_24dp);
            b.title = "...";
            bookmarks.add(b);
        }
        Cursor cursor = bookmarksSQLite.getBookmarks();
        while (cursor.moveToNext()) {
            BookmarksItem b = new BookmarksItem();
            b.type = cursor.getString(cursor.getColumnIndex("type"));
            b.title = cursor.getString(cursor.getColumnIndex("title"));
            if (b.type.equals("folder")) {
                b.icon = getResources().getDrawable(R.drawable.ic_folder_24dp);
            } else {
                byte[] iconInBytes = cursor.getBlob(cursor.getColumnIndex("icon"));
                if (iconInBytes != null) {
                    Bitmap iconBitmap = BitmapFactory.decodeByteArray(iconInBytes, 0, iconInBytes
                            .length);
                    b.icon = new BitmapDrawable(getResources(), iconBitmap);
                } else {
                    b.icon = getResources().getDrawable(R.drawable.ic_bookmark_24dp);
                }
                b.url = cursor.getString(cursor.getColumnIndex("link"));
            }
            bookmarks.add(b);
        }
        cursor.close();
    }

    boolean isCurrentTableRoot() {
        return bookmarksSQLite.getCurrentTable().equals(getResources().getString(R.string
                .bookmarks_root_folder));
    }

    private class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkItem> {
        @NonNull
        @Override
        public BookmarkItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new BookmarkItem(inflater.inflate(R.layout.bookmark, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BookmarkItem holder, int position) {
            holder.bind(bookmarks.get(position));
        }

        @Override
        public int getItemCount() {
            return bookmarks.size();
        }

        class BookmarkItem extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView icon;
            private TextView title;
            private ImageView menu;

            BookmarkItem(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.bookmarkIcon);
                title = itemView.findViewById(R.id.bookmarkTitle);
                menu = itemView.findViewById(R.id.bookmarkMenu);
                itemView.setOnClickListener(this);
                menu.setOnClickListener(this);
            }

            void bind(BookmarksItem bookmark) {
                icon.setImageDrawable(bookmark.icon);
                title.setText(bookmark.title);
                if (bookmark.type.equals("upFolder")) {
                    itemView.findViewById(R.id.bookmarkMenu).setVisibility(View.GONE);
                } else {
                    itemView.findViewById(R.id.bookmarkMenu).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    switch (bookmarks.get(getAdapterPosition()).type) {
                        case "upFolder":
                            bookmarksSQLite.setCurrentTable(bookmarksSQLite.getCurrentTable()
                                    .substring(0, bookmarksSQLite.getCurrentTable().lastIndexOf
                                            ("_")));
                            loadBookmarksData();
                            notifyDataSetChanged();
                            break;
                        case "folder":
                            if (isCurrentTableRoot()) {
                                bookmarksSQLite.setCurrentTable(bookmarksSQLite.getCurrentTable() +
                                        "_" + (getAdapterPosition() + 1));
                                loadBookmarksData();
                                notifyDataSetChanged();
                            } else {
                                bookmarksSQLite.setCurrentTable(bookmarksSQLite.getCurrentTable() +
                                        "_" + getAdapterPosition());
                                loadBookmarksData();
                                notifyDataSetChanged();
                            }
                            break;
                        case "link":
                            getLMvdActivity().browserClicked();
                            getLMvdActivity().getBrowserManager().newWindow(bookmarks.get
                                    (getAdapterPosition()).url);
                            break;
                    }
                } else if (v == menu) {
                    PopupMenu bookmarksMenu = new PopupMenu(getActivity(), v, Gravity.END);
                    bookmarksMenu.getMenu().add(Menu.NONE, Menu.NONE, 0, "Rename");
                    bookmarksMenu.getMenu().add(Menu.NONE, Menu.NONE, 1, "Copy");
                    bookmarksMenu.getMenu().add(Menu.NONE, Menu.NONE, 2, "Cut");
                    bookmarksMenu.getMenu().add(Menu.NONE, Menu.NONE, 4, "Delete");

                    if (!bookmarksClipboardManager.isClipboardEmpty() &&
                            bookmarksClipboardManager.getClipBoard().type.equals(bookmarks.get
                                    (getAdapterPosition()).type)) {
                        bookmarksMenu.getMenu().add(Menu.NONE, Menu.NONE, 3, "Paste");
                    }

                    bookmarksMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getTitle().toString()) {
                                case "Rename":
                                    final int position;
                                    if (isCurrentTableRoot()) {
                                        position = getAdapterPosition() + 1;
                                    } else {
                                        position = getAdapterPosition();
                                    }
                                    new RenameDialog(getActivity(), title.getText().toString()) {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {

                                        }

                                        @Override
                                        public void onOK(String newName) {

                                            bookmarksSQLite.renameBookmarkTitle(position, newName);
                                            bookmarks.get(getAdapterPosition()).title = newName;
                                            notifyItemChanged(getAdapterPosition());
                                        }
                                    };
                                    break;
                                case "Copy":
                                    if (isCurrentTableRoot()) {
                                        bookmarksClipboardManager.copy(getAdapterPosition() + 1);
                                    } else {
                                        bookmarksClipboardManager.copy(getAdapterPosition());
                                    }
                                    pasteButton.setVisibility(View.VISIBLE);
                                    break;
                                case "Cut":
                                    if (isCurrentTableRoot()) {
                                        bookmarksClipboardManager.cut(getAdapterPosition() + 1);
                                    } else {
                                        bookmarksClipboardManager.cut(getAdapterPosition());
                                    }
                                    pasteButton.setVisibility(View.VISIBLE);
                                    break;
                                case "Paste":
                                    boolean pasted;
                                    if (isCurrentTableRoot()) {
                                        pasted = bookmarksClipboardManager.paste(getAdapterPosition
                                                () + 1);
                                    } else {
                                        pasted = bookmarksClipboardManager.paste(getAdapterPosition
                                                ());
                                    }

                                    if (!pasted) {
                                        Toast.makeText(getActivity(), "Bookmark to move no " +
                                                "longer exist", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadBookmarksData();
                                        notifyDataSetChanged();
                                    }

                                    if (bookmarksClipboardManager.isClipboardEmpty()) {
                                        pasteButton.setVisibility(View.GONE);
                                    }

                                    break;
                                case "Delete":
                                    new AlertDialog.Builder(getActivity())
                                            .setMessage("Delete?")
                                            .setPositiveButton("YES", new DialogInterface
                                                    .OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (isCurrentTableRoot()) {
                                                        bookmarksSQLite.delete(getAdapterPosition() + 1);
                                                    } else {
                                                        bookmarksSQLite.delete(getAdapterPosition());
                                                    }
                                                    loadBookmarksData();
                                                    notifyDataSetChanged();
                                                    if (bookmarksClipboardManager.isClipboardEmpty()) {
                                                        pasteButton.setVisibility(View.GONE);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .create()
                                            .show();
                                    break;
                            }
                            return true;
                        }
                    });

                    bookmarksMenu.show();
                }
            }
        }
    }
}
