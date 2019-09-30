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

import android.graphics.Typeface
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding

class BrowserActionBarUpdater(
        private val activity: BrowserActivity,
        private val binding: ActivityBrowserBinding) {

    var customTitleView: View? = null; private set

    fun update() {
        setupActionBar()
        configureActionBar()
    }

    private fun setupActionBar() {
        val actionBar = binding.mainContentBrowser.browserToolbar
        activity.setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawerBrowser.openDrawer(GravityCompat.START) }
        actionBar.setOnMenuItemClickListener(activity.menuItemClickListener)
    }

    private fun configureActionBar() {
        customTitleView = View.inflate(activity, R.layout.browser_toolbar_custom_view, null)
        activity.supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            customView = customTitleView
        }
    }

    fun setupOptionsMenu(menu: Menu?) {

        activity.menuInflater.inflate(R.menu.browser_menu, menu)

        val switchMenuItem = menu?.findItem(R.id.browser_menu_switch_window)
        val switchView = TextView(activity)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val textColor = ResourcesCompat.getColor(activity.resources, R.color.yellow, null)
        switchView.apply {
            text = "1"
            setTypeface(this.typeface, Typeface.BOLD)
            setTextColor(textColor)
            background = AppCompatResources.getDrawable(context, R.drawable.switch_window_icon)
            layoutParams = params
            setOnClickListener { openWebViewSwitcherSheet() }
        }
        switchMenuItem?.actionView = switchView

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        activity.webViewsController.onUpdateWebViewsCountIndicator = { count -> switchView.text = "$count" }
    }

    private fun openWebViewSwitcherSheet() {
        if (!activity.switcherSheet.isAdded) {
            activity.switcherSheet.show(activity.supportFragmentManager, null)
        }
    }
}