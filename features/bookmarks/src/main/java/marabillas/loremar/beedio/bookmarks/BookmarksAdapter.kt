/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.bookmarks

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.extensions.imageView
import marabillas.loremar.beedio.base.extensions.textView
import javax.inject.Inject

class BookmarksAdapter @Inject constructor() : RecyclerView.Adapter<BookmarksAdapter.BookmarksViewHolder>() {
    var bookmarks: List<BookmarksItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bookmarks_item, parent, false)
        return BookmarksViewHolder(view)
    }

    override fun getItemCount() = bookmarks.count()

    override fun onBindViewHolder(holder: BookmarksViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    inner class BookmarksViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView by lazy { itemView.textView(R.id.bookmarks_item_textview) }

        fun bind(bookmark: BookmarksItem) {
            textView.setCompoundDrawablesWithIntrinsicBounds(bookmark.icon, null, null, null)
            textView.text = bookmark.title
            itemView.imageView(R.id.bookmarks_item_options).isVisible = bookmark.type != "upFolder"
        }
    }
}

data class BookmarksItem(
        val type: String,
        val icon: Drawable,
        val title: String,
        val url: String? = null
)