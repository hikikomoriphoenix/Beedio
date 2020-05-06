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

package marabillas.loremar.beedio.base.extensions

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

/* View */
fun View.textView(resId: Int): TextView = findViewById(resId)
fun View.imageView(resId: Int): ImageView = findViewById(resId)
fun View.recyclerView(resId: Int): RecyclerView = findViewById(resId)
fun View.bottomAppBar(resId: Int): BottomAppBar = findViewById(resId)
fun View.toolbar(resId: Int): Toolbar = findViewById(resId)
fun View.constraintLayout(resId: Int): ConstraintLayout = findViewById(resId)

/* Activity */
fun Activity.rootView(): View = findViewById(android.R.id.content)
fun Activity.textView(resId: Int): TextView = findViewById(resId)
fun Activity.imageView(resId: Int): ImageView = findViewById(resId)
fun Activity.toolbar(resId: Int): Toolbar = findViewById(resId)

/* Fragment */
fun Fragment.textView(resId: Int): TextView? = view?.findViewById(resId)
fun Fragment.imageView(resId: Int): TextView? = view?.findViewById(resId)
fun Fragment.toolbar(resId: Int): Toolbar? = view?.findViewById(resId)
fun Fragment.recyclerView(resId: Int): RecyclerView? = view?.findViewById(resId)
fun Fragment.floatingActionButton(resId: Int): FloatingActionButton? = view?.findViewById(resId)
fun Fragment.constraintLayout(resId: Int): ConstraintLayout? = view?.findViewById(resId)