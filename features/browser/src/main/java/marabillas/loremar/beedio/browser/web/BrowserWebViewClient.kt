package marabillas.loremar.beedio.browser.web

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import marabillas.loremar.beedio.browser.listeners.OnWebPageChangedListener
import javax.inject.Inject

class BrowserWebViewClient @Inject constructor() : WebViewClient() {

    var onWebPageChangedListener: OnWebPageChangedListener? = null

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onWebPageChangedListener?.onWebPageChanged(view, url, favicon)
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        onWebPageChangedListener?.onWebPageChanged(view, url, null)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onWebPageChangedListener?.onWebPageChanged(view, url, null)
    }

}