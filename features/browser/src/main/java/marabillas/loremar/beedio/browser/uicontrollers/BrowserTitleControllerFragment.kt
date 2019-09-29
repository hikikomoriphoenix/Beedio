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

import android.webkit.WebView
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleState
import javax.inject.Inject

class BrowserTitleControllerFragment @Inject constructor() : DaggerFragment(), TitleControllerInterface {
    var titleState: BrowserTitleState? = null

    override fun updateTitle(title: String?, url: String?) {
        titleState?.title = title
        titleState?.url = url
    }

    override fun updateTitle(title: String?) {
        titleState?.title = title
    }

    override fun updateTitle(webView: WebView?, title: String?, url: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}