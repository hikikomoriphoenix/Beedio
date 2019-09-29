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

package marabillas.loremar.beedio.home

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class HomeRecommendedCallback @Inject constructor() : BottomSheetBehavior.BottomSheetCallback() {
    private val onShowHeaderTransition = Slide(Gravity.TOP)

    var actionWhenHidden = { }

    override fun onSlide(p0: View, p1: Float) {
    }

    override fun onStateChanged(p0: View, p1: Int) {
        when (p1) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                TransitionManager.beginDelayedTransition(p0.parent as ViewGroup, onShowHeaderTransition)
                p0.findViewById<View>(R.id.home_recommended_header).visibility = View.VISIBLE
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                p0.findViewById<View>(R.id.home_recommended_header).visibility = View.GONE
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                p0.findViewById<View>(R.id.home_recommended_header).visibility = View.GONE
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                p0.findViewById<View>(R.id.home_recommended_header).visibility = View.GONE
                actionWhenHidden()
            }
            BottomSheetBehavior.STATE_DRAGGING -> {
                p0.findViewById<View>(R.id.home_recommended_header).visibility = View.GONE
            }
            BottomSheetBehavior.STATE_SETTLING -> {
            }
        }
    }
}