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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import dagger.android.support.DaggerFragment
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.viewmodel.BrowserTitleState
import javax.inject.Inject

class WebViewsControllerFragment @Inject constructor() : DaggerFragment(), WebPageNavigatorInterface,
        WebViewSwitcherInterface, TitleControllerInterface {

    var titleState: BrowserTitleState? = null
    var onUpdateWebViewsCountIndicator: (Int) -> Unit = { }

    private val _webViews = mutableListOf<WebView>()
    private var activeWebView: WebView? = null
    private var webViewsContainer: FrameLayout? = null

    override val webViews: MutableList<WebView>
        get() = _webViews

    override val activeWebViewIndex: Int
        get() = _webViews.indexOf(activeWebView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (webViewsContainer == null) {
            webViewsContainer = FrameLayout(inflater.context)
            webViewsContainer?.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        return webViewsContainer
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val parentContainer = activity?.findViewById<ViewGroup>(R.id.browser_webview_containter)
        updateParent(webViewsContainer, parentContainer)

        if (_webViews.isEmpty()) {
            val url = activity?.intent?.getStringExtra("url")
            url?.let { addWebView(it) }
        }

    }

    private fun updateParent(view: View?, parent: ViewGroup?) {
        (view?.parent as ViewGroup?)?.removeView(view)
        parent?.addView(view)
    }

    var webChromClient: WebChromeClient? = null
        set(value) {
            field = value
            _webViews.forEach { it.webChromeClient = value }
        }

    var webViewClient: WebViewClient? = null
        set(value) {
            field = value
            _webViews.forEach { it.webViewClient = value }
        }

    override fun goBack() {
        activeWebView?.goBack()
    }

    override fun goForward() {
        activeWebView?.goForward()
    }

    override fun reloadPage() {
        activeWebView?.reload()
    }

    override fun newWebView(url: String) {
        addWebView(url)
        updateWebViewsCountIndicator()
    }

    override fun switchWebView(index: Int) {
        activeWebView?.visibility = View.GONE

        activeWebView = _webViews[index]
        (activeWebView?.parent as ViewGroup?)?.removeView(activeWebView)
        webViewsContainer?.addView(activeWebView)

        activeWebView?.let {
            it.visibility = View.VISIBLE
            updateTitle(it, it.title, it.url)
        }

        updateWebViewsCountIndicator()
    }

    override fun closeWebView() {
        (activeWebView?.parent as ViewGroup?)?.removeView(activeWebView)
        val index = _webViews.indexOf(activeWebView)
        _webViews.removeAt(index)
        if (_webViews.isNotEmpty())
            if (index > 0) {
                switchWebView(index - 1)
            } else {
                switchWebView(index)
            }
        else
            activity?.finish()

        updateWebViewsCountIndicator()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun addWebView(url: String) {
        this.also { webViewsController ->
            activeWebView?.visibility = View.GONE
            activeWebView = WebView(activity).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                webViewClient = webViewsController.webViewClient
                webChromClient = webViewsController.webChromClient
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowUniversalAccessFromFileURLs = true
                settings.domStorageEnabled = true
                loadUrl(url)
            }
        }

        webViewsContainer?.addView(activeWebView)
        activeWebView?.let { _webViews.add(it) }
    }

    private fun updateWebViewsCountIndicator() {
        onUpdateWebViewsCountIndicator(_webViews.count())
    }

    override fun updateTitle(title: String?, url: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateTitle(title: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateTitle(webView: WebView?, title: String?, url: String?) {
        if (webView == activeWebView) {
            titleState?.title = title
            url?.let { titleState?.url = it }
        }
    }
}