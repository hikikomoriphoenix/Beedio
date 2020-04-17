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
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.extensions.imageView
import marabillas.loremar.beedio.base.extensions.textView
import marabillas.loremar.beedio.base.extensions.toPixels
import javax.inject.Inject

class BookmarksAdapter @Inject constructor() : RecyclerView.Adapter<BookmarksAdapter.BookmarksViewHolder>() {
    var bookmarks: List<BookmarksItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var itemEventListener: ItemEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bookmarks_item, parent, false)
        return BookmarksViewHolder(view)
    }

    override fun getItemCount() = bookmarks.count()

    override fun onBindViewHolder(holder: BookmarksViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    inner class BookmarksViewHolder(view: View) :
            RecyclerView.ViewHolder(view),
            View.OnClickListener,
            PopupMenu.OnMenuItemClickListener {

        private val textView by lazy { itemView.textView(R.id.bookmarks_item_textview) }
        private val options by lazy { itemView.imageView(R.id.bookmarks_item_options) }

        init {
            textView.setOnClickListener(this)
            options.setOnClickListener(this)
        }

        fun bind(bookmark: BookmarksItem) {
            textView.apply {
                setCompoundDrawablesWithIntrinsicBounds(bookmark.icon, null, null, null)
                compoundDrawablePadding = 8.toPixels(resources)
                text = bookmark.title
                gravity = Gravity.CENTER_VERTICAL
            }
            itemView.imageView(R.id.bookmarks_item_options).isVisible = bookmark.type != "upFolder"
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.bookmarks_item_textview -> {
                    itemEventListener?.onBookmarksItemClick(bookmarks[adapterPosition], adapterPosition)
                }
                R.id.bookmarks_item_options -> {
                    PopupMenu(itemView.context, options).apply {
                        menu.add(itemView.resources.getString(R.string.rename))
                        menu.add(itemView.resources.getString(R.string.copy))
                        menu.add(itemView.resources.getString(R.string.cut))
                        menu.add(itemView.resources.getString(R.string.paste))
                        menu.add(itemView.resources.getString(R.string.delete))
                        show()
                    }.setOnMenuItemClickListener(this)
                }
            }
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item?.title) {
                itemView.resources.getString(R.string.rename) -> itemEventListener?.onShowRenameDialog(adapterPosition)
                itemView.resources.getString(R.string.copy) -> itemEventListener?.onCopyItem(adapterPosition)
                itemView.resources.getString(R.string.cut) -> itemEventListener?.onCutItem(adapterPosition)
                itemView.resources.getString(R.string.paste) -> itemEventListener?.onPasteItem(adapterPosition)
                itemView.resources.getString(R.string.delete) -> itemEventListener?.onDeleteItem(adapterPosition)
            }
            return true
        }
    }

    interface ItemEventListener {
        fun onBookmarksItemClick(bookmarksItem: BookmarksItem, position: Int)
        fun onShowRenameDialog(position: Int)
        fun onCopyItem(position: Int)
        fun onCutItem(position: Int)
        fun onPasteItem(position: Int)
        fun onDeleteItem(position: Int)
    }
}

data class BookmarksItem(
        val type: String,
        val icon: Drawable,
        val title: String,
        val url: String? = null
)