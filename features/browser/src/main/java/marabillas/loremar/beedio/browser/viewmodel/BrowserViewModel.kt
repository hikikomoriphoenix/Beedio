package marabillas.loremar.beedio.browser.viewmodel

import android.view.Gravity
import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import marabillas.loremar.beedio.base.mvvm.ObservableViewModel

class BrowserViewModel : ObservableViewModel(), BrowserTitleState, BrowserAppBarState, BrowserSearchWidgetState {

    private val titleData = MutableLiveData<String>()
    private val urlData = MutableLiveData<String>()
    private val appBarVisibilityData = MutableLiveData<Int>()
    private val searchWidgetContainerVisibilityData = MutableLiveData<Int>()
    private val searchWidgetGravityData = MutableLiveData<Int>()
    private val searchWidgetWidthData = MutableLiveData<Int>()
    private val searchEditTextVisibilityData = MutableLiveData<Int>()
    private val searchCloseBtnVisibilityData = MutableLiveData<Int>()

    init {
        appBarVisibilityData.value = View.VISIBLE
        searchWidgetContainerVisibilityData.value = View.GONE
        searchWidgetGravityData.value = Gravity.END
        searchWidgetWidthData.value = 0
        searchEditTextVisibilityData.value = View.INVISIBLE
        searchCloseBtnVisibilityData.value = View.INVISIBLE
    }

    override var title: String?
        get() = titleData.value
        set(value) {
            titleData.value = value
        }

    override var url: String?
        get() = urlData.value
        set(value) {
            urlData.value = value
        }

    override fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        titleData.observe(lifecycleOwner, observer)
    }

    override fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>) {
        urlData.observe(lifecycleOwner, observer)
    }

    @Bindable
    override fun getAppBarVisibility(): Int {
        return appBarVisibilityData.value ?: View.VISIBLE
    }

    override fun setAppBarVisibility(value: Int) {
        appBarVisibilityData.value = value
    }

    @Bindable
    override fun getSearchWidgetContainerVisibility(): Int {
        return searchWidgetContainerVisibilityData.value ?: View.GONE
    }

    override fun setSearchWidgetContainerVisibility(value: Int) {
        searchWidgetContainerVisibilityData.value = value
    }

    @Bindable
    override fun getSearchWidgetGravity(): Int {
        return searchWidgetGravityData.value ?: Gravity.END
    }

    override fun setSearchWidgetGravity(value: Int) {
        searchWidgetGravityData.value = value
    }

    @Bindable
    override fun getSearchWidgetWidth(): Int {
        return searchWidgetWidthData.value ?: 0
    }

    override fun setSearchWidgetWidth(value: Int) {
        searchWidgetWidthData.value = value
    }

    @Bindable
    override fun getSearchEditTextVisibility(): Int {
        return searchEditTextVisibilityData.value ?: View.INVISIBLE
    }

    override fun setSearchEditTextVisibility(value: Int) {
        searchEditTextVisibilityData.value = value
    }

    @Bindable
    override fun getSearchCloseBtnVisibility(): Int {
        return searchCloseBtnVisibilityData.value ?: View.INVISIBLE
    }

    override fun setSearchCloseBtnVisibility(value: Int) {
        searchCloseBtnVisibilityData.value = value
    }
}