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

package marabillas.loremar.beedio.browser.activity

import android.widget.TextView
import androidx.lifecycle.Observer
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.viewmodel.BrowserAppBarStateVM
import marabillas.loremar.beedio.browser.viewmodel.BrowserSearchWidgetStateVM
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleStateVM

class BrowserViewModelBinder(
        private val activity: BrowserActivity,
        private val actionBarUpdater: BrowserActionBarUpdater,
        private val appBarStateVM: BrowserAppBarStateVM,
        private val searchWidgetStateVM: BrowserSearchWidgetStateVM,
        private val titleStateVM: BrowserTitleStateVM,
        private val binding: ActivityBrowserBinding) {

    fun bind() {
        val titleView = actionBarUpdater.customTitleView?.findViewById<TextView>(R.id.browser_title)
        val urlView = actionBarUpdater.customTitleView?.findViewById<TextView>(R.id.browser_url)
        titleStateVM.observeTitle(activity, Observer { titleView?.text = it })
        titleStateVM.observeUrl(activity, Observer { urlView?.text = it })
        binding.mainContentBrowser.appBarState = appBarStateVM
        binding.mainContentBrowser.searchWidgetState = searchWidgetStateVM
        binding.mainContentBrowser.searchWidgetListener = activity.uiListener
    }
}