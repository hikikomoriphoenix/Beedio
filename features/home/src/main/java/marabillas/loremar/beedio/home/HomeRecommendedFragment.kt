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

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeRecommendedFragment @Inject constructor() : BottomSheetDialogFragment() {
    lateinit var activity: HomeActivity

    @Inject
    lateinit var homeRecommendedCallback: HomeRecommendedCallback

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        activity = getActivity() as HomeActivity
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.setContentView(R.layout.home_recommended)
        val view = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (view != null) {
            val params = view.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = params
            val behavior = BottomSheetBehavior.from(view)
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            behavior.setBottomSheetCallback(homeRecommendedCallback)
            homeRecommendedCallback.actionWhenHidden = this::dismiss
        }
    }
}