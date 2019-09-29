package marabillas.loremar.beedio.browser.activity

class BrowserListenersUpdater(private val activity: BrowserActivity) {

    fun update() {
        activity.browserWebViewClient.onWebPageChangedListener = activity.uiListener
        activity.browserWebChromeClient.titleRecievedListener = activity.uiListener

        activity.uiListener.titleController = activity.webViewsController
        activity.uiListener.searchWidgetController = activity.searchWidgeController
        activity.menuItemClickListener.webPageNavigator = activity.webViewsController
        activity.menuItemClickListener.searchWidgetController = activity.searchWidgeController
        activity.menuItemClickListener.webViewSwitcher = activity.webViewsController
    }
}