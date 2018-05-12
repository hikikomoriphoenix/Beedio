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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import marabillas.loremar.lmvideodownloader.LMvdActivity;
import marabillas.loremar.lmvideodownloader.LMvdFragment;
import marabillas.loremar.lmvideodownloader.R;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class Bookmarks extends LMvdFragment implements LMvdActivity.OnBackPressedListener {
    private List<BookmarksItem> bookmarks;
    private BookmarksSQLite bookmarksSQLite;

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
        View view = inflater.inflate(R.layout.bookmarks, container, false);
        RecyclerView bookmarksView = view.findViewById(R.id.bookmarks);

        getLMvdActivity().setOnBackPressedListener(this);

        bookmarksSQLite = new BookmarksSQLite(getActivity());

        loadBookmarksData();

        bookmarksView.setAdapter(new BookmarksAdapter());
        bookmarksView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bookmarksView.setHasFixedSize(true);
        bookmarksView.addItemDecoration(Utils.createDivider(getActivity()));

        return view;
    }

    private void loadBookmarksData() {
        bookmarks = new ArrayList<>();
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
                bookmarks.add(b);
            }
        }
        cursor.close();
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

        class BookmarkItem extends RecyclerView.ViewHolder {
            private ImageView icon;
            private TextView title;

            BookmarkItem(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.bookmarkIcon);
                title = itemView.findViewById(R.id.bookmarkTitle);
            }

            void bind(BookmarksItem bookmark) {
                icon.setImageDrawable(bookmark.icon);
                title.setText(bookmark.title);
            }
        }
    }
}
