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
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.android.support.AndroidSupportInjection
import marabillas.loremar.beedio.base.extensions.toPixels
import marabillas.loremar.beedio.base.mvvm.MainViewModel
import marabillas.loremar.beedio.base.web.WebNavigation
import javax.inject.Inject

class HomeRecommendedFragment : BottomSheetDialogFragment(), HomeRecommendedAdapter.OnWebsiteSelectedListener {
    @Inject
    lateinit var homeRecommendedAdapter: HomeRecommendedAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var webNavigation: WebNavigation

    private lateinit var mainViewModel: MainViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetTheme)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.setContentView(R.layout.home_recommended)
        val view = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (view != null) {
            val params = view.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = params
        }

        dialog.findViewById<View>(R.id.home_recommended_header).apply {
            background = MaterialShapeDrawable().apply {
                fillColor = ColorStateList.valueOf(Color.WHITE)
                shapeAppearanceModel = ShapeAppearanceModel.Builder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, 20.toPixels(resources).toFloat())
                        .setTopRightCorner(CornerFamily.ROUNDED, 20.toPixels(resources).toFloat())
                        .build()
            }
        }

        dialog.findViewById<RecyclerView>(R.id.recycler_home_recommended).apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
            adapter = homeRecommendedAdapter
        }

        homeRecommendedAdapter.onWebsiteSelectedListener = this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            mainViewModel = ViewModelProvider(it, viewModelFactory).get(MainViewModel::class.java)
        }
    }

    override fun onWebsiteSelected(url: String) {
        dismiss()
        val validatedUrl = webNavigation.navigateTo(url)
        mainViewModel.goToBrowser(validatedUrl)
    }

    companion object {
        private const val GRID_SPAN_COUNT = 3
    }
}