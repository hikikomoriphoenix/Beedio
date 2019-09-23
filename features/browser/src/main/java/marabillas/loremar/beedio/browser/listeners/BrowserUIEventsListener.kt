package marabillas.loremar.beedio.browser.listeners

import android.graphics.Bitmap
import android.webkit.WebView
import marabillas.loremar.beedio.base.di.ActivityScope
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerInterface
import marabillas.loremar.beedio.browser.uicontrollers.TitleControllerInterface
import javax.inject.Inject

@ActivityScope
class BrowserUIEventsListener @Inject constructor() : OnWebPageChangedListener,
        OnWebPageTitleRecievedListener, BrowserSearchWidgetListener {

    var titleController: TitleControllerInterface? = null
    var searchWidgetController: BrowserSearchWidgetControllerInterface? = null

    override fun onWebPageChanged(title: String?, url: String?, favicon: Bitmap?) {
        titleController?.updateTitle(title, url)
    }

    override fun onWebPageChanged(webView: WebView?, url: String?, favicon: Bitmap?) {
        titleController?.updateTitle(webView, webView?.title, url)
    }

    override fun onWebPageTitleRecieved(title: String?) {
        titleController?.updateTitle(title)
    }

    override fun onWebPageTitleRecieved(webView: WebView?, title: String?) {
        titleController?.updateTitle(webView, title, null)
    }

    override fun onSearchCloseBtnClicked() {
        searchWidgetController?.onCloseBtnClicked()
    }
}