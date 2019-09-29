package marabillas.loremar.beedio.browser.activity

import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel

class BrowserControllersUpdater(private val activity: BrowserActivity, private val viewModel: BrowserViewModel) {

    fun update() {
        setupTitleController()
        setupWebViewsController()
        setSearchWidgetController()
    }

    private fun setupTitleController() {
        activity.titleController.titleState = viewModel
        activity.supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, activity.titleController)
                .commit()
    }

    private fun setupWebViewsController() {
        activity.webViewsController.webChromClient = activity.browserWebChromeClient
        activity.webViewsController.webViewClient = activity.browserWebViewClient
        activity.webViewsController.titleState = viewModel

        if (activity.supportFragmentManager.findFragmentByTag("WebViewsControllerFragment") == null) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .add(activity.webViewsController, "WebViewsControllerFragment")
                    .commit()
        }
    }

    private fun setSearchWidgetController() {
        activity.searchWidgeController.webViewSwitcher = activity.webViewsController
        activity.searchWidgeController.webNavigation = activity.webNavigation

        activity.supportFragmentManager
                .beginTransaction()
                .add(activity.searchWidgeController, null)
                .commit()
    }
}