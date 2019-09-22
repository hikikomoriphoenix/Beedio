package marabillas.loremar.beedio.browser

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import marabillas.loremar.beedio.browser.databinding.ActivityBrowserBinding
import marabillas.loremar.beedio.browser.listeners.BrowserMenuItemClickListener
import marabillas.loremar.beedio.browser.listeners.BrowserUIEventsListener
import marabillas.loremar.beedio.browser.uicontrollers.BrowserTitleControllerFragment
import marabillas.loremar.beedio.browser.uicontrollers.WebPageNavigatorFragment
import marabillas.loremar.beedio.browser.viewmodel.BrowserViewModel
import javax.inject.Inject

class BrowserActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var viewModel: BrowserViewModel
    private lateinit var customTitleView: View

    @Inject
    lateinit var titleController: BrowserTitleControllerFragment
    @Inject
    lateinit var webPageNavigator: WebPageNavigatorFragment
    @Inject
    lateinit var browserWebViewClient: BrowserWebViewClient
    @Inject
    lateinit var browserWebChromeClient: BrowserWebChromeClient
    @Inject
    lateinit var uiListener: BrowserUIEventsListener
    @Inject
    lateinit var menuItemClickListener: BrowserMenuItemClickListener

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
    }

    private fun setupControllers() {
        setupTitleController()
        setupWebPageNavigator()
    }

    private fun setupTitleController() {
        titleController.titleState = viewModel
        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, titleController)
                .commit()
    }

    private fun setupWebPageNavigator() {
        supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, webPageNavigator)
                .commit()
    }

    private fun setupListeners() {
        browserWebViewClient.onWebPageChangedListener = uiListener
        browserWebChromeClient.titleRecievedListener = uiListener

        uiListener.titleController = titleController
        menuItemClickListener.webPageNavigator = webPageNavigator
    }

    override fun onStart() {
        super.onStart()
        val url = intent.getStringExtra("url")
        setupWebView(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        binding.mainContentBrowser
                .browserWebview
                .apply {
                    settings.javaScriptEnabled = true
                    webChromeClient = browserWebChromeClient
                    webViewClient = browserWebViewClient
                    loadUrl(url)
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.browser_menu, menu)
        val switchMenuItem = menu?.findItem(R.id.browser_switch_tool)
        val switchView = TextView(this)
        val params = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        switchView.apply {
            text = "6"
            setTextColor(Color.WHITE)
            background = AppCompatResources.getDrawable(context, R.drawable.switch_window_icon)
            layoutParams = params
        }
        switchMenuItem?.actionView = switchView

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }


}