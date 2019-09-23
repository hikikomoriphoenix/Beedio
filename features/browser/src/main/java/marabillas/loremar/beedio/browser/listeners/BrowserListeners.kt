package marabillas.loremar.beedio.browser.listeners

import android.graphics.Bitmap
import android.webkit.WebView

interface OnWebPageChangedListener {
    fun onWebPageChanged(title: String?, url: String?, favicon: Bitmap?)

    fun onWebPageChanged(webView: WebView?, url: String?, favicon: Bitmap?)
}

interface OnWebPageTitleRecievedListener {
    fun onWebPageTitleRecieved(title: String?)

    fun onWebPageTitleRecieved(webView: WebView?, title: String?)
}

interface BrowserSearchWidgetListener {
    fun onSearchCloseBtnClicked()
}