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

package marabillas.loremar.beedio.download.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.download.viewmodels.InactiveVM
import javax.inject.Inject

class SourcePageFragment @Inject constructor() : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var inactiveVM: InactiveVM

    private val _index by lazy { arguments?.getInt(ARG_INDEX) }
    private val _page by lazy { arguments?.getString(ARG_PAGE) }

    companion object {
        const val ARG_INDEX = "arg_index"
        const val ARG_PAGE = "arg_page"
        const val REQUEST_CODE_REFRESHED = 0
        const val RESULT_INDEX = "result_index"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            settings.javaScriptEnabled = true
            webViewClient = (SourcePageClient())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.apply {
            inactiveVM = ViewModelProvider(this::getViewModelStore, viewModelFactory).get(InactiveVM::class.java)
        }
    }

    override fun onStart() {
        super.onStart()
        _page?.let { (view as WebView).loadUrl(it) }
    }

    private fun dismissFragment() {
        requireFragmentManager().popBackStack()
    }

    inner class SourcePageClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return true
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            if (url != null) {
                _index?.let {
                    inactiveVM.analyzeUrlForFreshLink(url, it) {
                        dismissFragment()
                        targetFragment?.onActivityResult(
                                REQUEST_CODE_REFRESHED,
                                RESULT_OK,
                                Intent().apply {
                                    putExtra(RESULT_INDEX, it)
                                }
                        )
                    }
                }
            }
        }
    }
}