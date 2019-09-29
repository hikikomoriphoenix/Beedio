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

package marabillas.loremar.beedio.browser.uicontrollers

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import marabillas.loremar.beedio.browser.R
import javax.inject.Inject
import kotlin.math.roundToInt

class WebViewSwitcherSheetAdapter @Inject constructor() : RecyclerView.Adapter<WebViewSwitcherSheetViewHolder>() {

    var actionOnItemSelect: (Int) -> Unit = { }

    private var webViews: List<WebView>? = null
    private var activeIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebViewSwitcherSheetViewHolder {

        val density = parent.context.resources.displayMetrics.density

        val itemView = AppCompatTextView(parent.context).apply {
            setPadding((8 * density).roundToInt())
            compoundDrawablePadding = (16 * density).roundToInt()
            background = ResourcesCompat.getDrawable(
                    parent.context.resources, R.drawable.yellow_ripple, null)
            setTextColor(Color.WHITE)
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }

        return WebViewSwitcherSheetViewHolder(itemView, actionOnItemSelect)
    }

    override fun getItemCount(): Int {
        return webViews?.count() ?: 0
    }

    override fun onBindViewHolder(holder: WebViewSwitcherSheetViewHolder, position: Int) {
        webViews?.let {
            if (position == activeIndex) {
                holder.bind(it[position], true)
            } else {
                holder.bind(it[position])
            }
        }
    }

    fun update(webViews: List<WebView>, activeIndex: Int) {
        this.webViews = webViews
        this.activeIndex = activeIndex
        notifyDataSetChanged()
    }
}

class WebViewSwitcherSheetViewHolder(view: View, private val actionOnItemSelect: (Int) -> Unit) : RecyclerView.ViewHolder(view) {

    fun bind(webView: WebView, isActive: Boolean = false) {
        if (itemView is TextView) {
            val faviconDrawable = if (webView.favicon != null) {

                val iconSize = (24 * itemView.resources.displayMetrics.density)
                        .roundToInt()
                val scaledFavicon = Bitmap.createScaledBitmap(
                        webView.favicon, iconSize, iconSize, true)
                BitmapDrawable(itemView.resources, scaledFavicon)

            } else {
                ResourcesCompat.getDrawable(
                        itemView.resources,
                        R.drawable.ic_missing_favicon_placeholder,
                        null)
            }

            itemView.apply {
                text = webView.title
                setCompoundDrawablesWithIntrinsicBounds(
                        faviconDrawable, null, null, null)
            }

            if (isActive) {
                val activeColor = ResourcesCompat.getColor(
                        itemView.context.resources, R.color.yellow, null)
                itemView.apply {
                    background = ColorDrawable(activeColor)
                    setTextColor(Color.BLACK)
                }
            }

            itemView.setOnClickListener { actionOnItemSelect(adapterPosition) }
        }
    }
}