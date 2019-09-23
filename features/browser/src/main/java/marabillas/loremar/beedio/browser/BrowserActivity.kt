package marabillas.loremar.beedio.browser

import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.base.web.WebNavigation
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserSearchWidgetControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewSwitcherSheetFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebViewsControllerFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel
import javax.inject.Inject

class BrowserActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var viewModel: BrowserViewModel
    private lateinit var customTitleView: View

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this).get(BrowserViewModel::class.java)

        setupActionBar()
        configureActionBar()

        bindViewModelOnUI()
        setupControllers()
        setupListeners()
    }

    private fun setupActionBar() {
        val actionBar = binding.mainContentBrowser.browserToolbar
        setSupportActionBar(actionBar)
        actionBar.setNavigationOnClickListener { binding.navDrawerBrowser.openDrawer(GravityCompat.START) }
        actionBar.setOnMenuItemClickListener(menuItemClickListener)
    }

    private fun configureActionBar() {
        customTitleView = View.inflate(this, R.layout.browser_toolbar_custom_view, null)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            customView = customTitleView
        }
    }

    private fun bindViewModelOnUI() {
        val titleView = customTitleView.findViewById<TextView>(R.id.browser_title)
        val urlView = customTitleView.findViewById<TextView>(R.id.browser_url)
        viewModel.observeTitle(this, Observer { titleView.text = it })
        viewModel.observeUrl(this, Observer { urlView.text = it })
        binding.mainContentBrowser.appBarState = viewModel
        binding.mainContentBrowser.searchWidgetState = viewModel
        binding.mainContentBrowser.searchWidgetListener = uiListener
    }

    private fun setupControllers() {
        setupTitleController()
        setupWebViewsController()
        setSearchWidgetController()
    }

    private fun setupTitleController() {
        titleController.titleState = viewModel
        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, titleController)
                .commit()
    }

    private fun setupWebViewsController() {
        webViewsController.webChromClient = browserWebChromeClient
        webViewsController.webViewClient = browserWebViewClient
        webViewsController.titleState = viewModel

        if (supportFragmentManager.findFragmentByTag("WebViewsControllerFragment") == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(webViewsController, "WebViewsControllerFragment")
                    .commit()
        }
    }

    private fun setSearchWidgetController() {
        searchWidgeController.webViewSwitcher = webViewsController
        searchWidgeController.webNavigation = webNavigation

        supportFragmentManager
                .beginTransaction()
                .add(searchWidgeController, null)
                .commit()
    }

    private fun setupListeners() {
        browserWebViewClient.onWebPageChangedListener = uiListener
        browserWebChromeClient.titleRecievedListener = uiListener

        uiListener.titleController = webViewsController
        uiListener.searchWidgetController = searchWidgeController
        menuItemClickListener.webPageNavigator = webViewsController
        menuItemClickListener.searchWidgetController = searchWidgeController
        menuItemClickListener.webViewSwitcher = webViewsController
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.browser_menu, menu)

        val switchMenuItem = menu?.findItem(R.id.browser_menu_switch_window)
        val switchView = TextView(this)
        val params = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        val textColor = ResourcesCompat.getColor(resources, R.color.yellow, null)
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

        webViewsController.onUpdateWebViewsCountIndicator = { count -> switchView.text = "$count" }

        return true
    }

    private fun openWebViewSwitcherSheet() {
        switcherSheet.webViewSwitcher = webViewsController
        if (!switcherSheet.isAdded) {
            switcherSheet.show(supportFragmentManager, null)
        }
    }

}