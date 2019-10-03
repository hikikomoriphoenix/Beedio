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

package marabillas.loremar.beedio.sharedui

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

object BindingAdapters {
    @BindingAdapter("android:visibility")
    @JvmStatic
    fun setVisibility(view: View, newValue: Int) {
        view.visibility = newValue
    }

    @BindingAdapter("app:changing_width")
    @JvmStatic
    fun setChangingWidth(view: View, newValue: Int) {
        val params = view.layoutParams
        params.width = newValue
        view.layoutParams = params
    }

    @BindingAdapter("android:layout_gravity")
    @JvmStatic
    fun setLayoutGravity(view: View, newValue: Int) {
        val params = view.layoutParams
        if (params is FrameLayout.LayoutParams) {
            params.gravity = newValue
        }
        view.layoutParams = params
    }

    @InverseBindingAdapter(attribute = "android:visibility")
    @JvmStatic
    fun getVisibility(view: View): Int {
        return view.visibility
    }

    @InverseBindingAdapter(attribute = "app:changing_width")
    @JvmStatic
    fun getChangingWidth(view: View): Int {
        return view.layoutParams.width
    }

    @InverseBindingAdapter(attribute = "android:layout_gravity")
    @JvmStatic
    fun getLayoutGravity(view: View): Int {
        val params = view.layoutParams
        return if (params is FrameLayout.LayoutParams) {
            params.gravity
        } else {
            Gravity.NO_GRAVITY
        }
    }

    @BindingAdapter("android:visibilityAttrChanged")
    @JvmStatic
    fun setGlobalLayoutListenerForVisibility(view: View, visibilityAttrChanged: InverseBindingListener?) {
        //view.viewTreeObserver.addOnGlobalLayoutListener { visibilityAttrChanged?.onChange() }
    }

    @BindingAdapter("app:changing_widthAttrChanged")
    @JvmStatic
    fun setGlobalLayoutListenerForChangingWidth(view: View, layoutWidthAttrChanged: InverseBindingListener?) {
        view.viewTreeObserver.addOnGlobalLayoutListener { layoutWidthAttrChanged?.onChange() }
    }

    @BindingAdapter("android:layout_gravityAttrChanged")
    @JvmStatic
    fun setGlobalLayoutListenerForLayoutGravity(view: View, layoutGravityAttrChanged: InverseBindingListener?) {
        view.viewTreeObserver.addOnGlobalLayoutListener { layoutGravityAttrChanged?.onChange() }
    }
}