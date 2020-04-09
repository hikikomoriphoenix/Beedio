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

package marabillas.loremar.beedio.browser.adapters

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.base.extensions.color
import marabillas.loremar.beedio.base.extensions.drawable
import marabillas.loremar.beedio.base.extensions.toPixels
import marabillas.loremar.beedio.browser.R
import javax.inject.Inject

class AddBookmarkAdapter @Inject constructor()
    : RecyclerView.Adapter<AddBookmarkAdapter.AddBookmarkViewHolder>() {

    var folders = listOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var itemEventListener: ItemEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddBookmarkViewHolder {
        val view = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            val verticalPadding = 4.toPixels(resources)
            val horizontalPadding = 8.toPixels(resources)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            val icon = resources.drawable(R.drawable.ic_folder_yellow_24dp)
            setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
            compoundDrawablePadding = 8.toPixels(resources)
            setTextColor(resources.color(R.color.white))
            gravity = Gravity.CENTER_VERTICAL
        }
        return AddBookmarkViewHolder(view)
    }

    override fun getItemCount(): Int = folders.count()

    override fun onBindViewHolder(holder: AddBookmarkViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    inner class AddBookmarkViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                itemEventListener?.onFolderClick(folders[adapterPosition], adapterPosition)
            }
        }

        fun bind(folder: String) {
            (itemView as TextView).text = folder
        }
    }

    interface ItemEventListener {
        fun onFolderClick(name: String, position: Int)
    }
}