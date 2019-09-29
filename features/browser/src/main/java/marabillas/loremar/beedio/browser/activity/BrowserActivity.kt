package marabillas.loremar.beedio.browser.activity

import android.os.Bundle
import android.view.Menu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.base.web.WebNavigation
import marabillas.loremar.beedio.browser.R
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel
import marabillas.loremar.beedio.browser.web.BrowserWebChromeClient
import marabillas.loremar.beedio.browser.web.BrowserWebViewClient
import javax.inject.Inject

class BrowserActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var titleController: BrowserTitleControllerFragment
    @Inject
    lateinit var webViewsController: WebViewsControllerFragment
    @Inject
    lateinit var searchWidgeController: BrowserSearchWidgetControllerFragment
    @Inject
    lateinit var switcherSheet: WebViewSwitcherSheetFragment
    @Inject
    lateinit var browserWebViewClient: BrowserWebViewClient
    @Inject
    lateinit var browserWebChromeClient: BrowserWebChromeClient
    @Inject
    lateinit var uiListener: BrowserUIEventsListener
    @Inject
    lateinit var menuItemClickListener: BrowserMenuItemClickListener
    @Inject
    lateinit var webNavigation: WebNavigation

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var viewModel: BrowserViewModel
    private lateinit var controllersUpdater: BrowserControllersUpdater
    private lateinit var actionBarUpdater: BrowserActionBarUpdater
    private lateinit var viewModelBinder: BrowserViewModelBinder
    private lateinit var listenersUpdater: BrowserListenersUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this).get(BrowserViewModel::class.java)

        actionBarUpdater = BrowserActionBarUpdater(this, binding)
        viewModelBinder = BrowserViewModelBinder(this, actionBarUpdater, viewModel, binding)
        controllersUpdater = BrowserControllersUpdater(this, viewModel)
        listenersUpdater = BrowserListenersUpdater(this)

        actionBarUpdater.update()
        viewModelBinder.bind()
        controllersUpdater.update()
        listenersUpdater.update()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        actionBarUpdater.setupOptionsMenu(menu)
        return true
    }

}