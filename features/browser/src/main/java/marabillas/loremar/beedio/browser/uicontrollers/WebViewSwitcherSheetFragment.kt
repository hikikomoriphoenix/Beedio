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

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import marabillas.loremar.beedio.browser.R
import javax.inject.Inject

class WebViewSwitcherSheetFragment @Inject constructor() : BottomSheetDialogFragment() {

    var webViewSwitcher: WebViewSwitcherInterface? = null

    @Inject
    lateinit var switcherSheetCallback: WebViewSwitcherSheetCallback
    @Inject
    lateinit var switcherSheetAdapter: WebViewSwitcherSheetAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.setContentView(R.layout.browser_webview_switcher_sheet)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            setupFullScreenSheet(it)
            setupBehavior(it)
        }

        setupRecyclerView()
    }

    private fun setupFullScreenSheet(sheet: View) {
        val params = sheet.layoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        sheet.layoutParams = params
    }

    private fun setupBehavior(sheet: View) {
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
            setBottomSheetCallback(switcherSheetCallback)
        }

        switcherSheetCallback.actionWhenHidden = this::dismiss
    }

    private fun setupRecyclerView() {
        dialog
                ?.findViewById<RecyclerView>(R.id.browser_webview_switcher_sheet_recyclerview)
                ?.apply {
                    adapter = switcherSheetAdapter
                    layoutManager = LinearLayoutManager(context)
                }
    }

    override fun onStart() {
        super.onStart()
        webViewSwitcher?.let {
            switcherSheetAdapter.update(it.webViews, it.activeWebViewIndex)
        }
        switcherSheetAdapter.actionOnItemSelect = this::onItemSelected
    }

    private fun onItemSelected(itemIndex: Int) {
        dismiss()
        webViewSwitcher?.switchWebView(itemIndex)
    }
}