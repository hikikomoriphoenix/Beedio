package marabillas.loremar.beedio.browser.viewmodel

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

interface BrowserTitleState {
    var title: String?
    var url: String?
    fun observeTitle(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
    fun observeUrl(lifecycleOwner: LifecycleOwner, observer: Observer<String?>)
}

interface BrowserAppBarState : Observable {
    @Bindable
    fun getAppBarVisibility(): Int

    fun setAppBarVisibility(value: Int)
}

interface BrowserSearchWidgetState : Observable {

    @Bindable
    fun getSearchWidgetContainerVisibility(): Int

    fun setSearchWidgetContainerVisibility(value: Int)

    @Bindable
    fun getSearchWidgetGravity(): Int

    fun setSearchWidgetGravity(value: Int)

    @Bindable
    fun getSearchWidgetWidth(): Int

    fun setSearchWidgetWidth(value: Int)

    @Bindable
    fun getSearchEditTextVisibility(): Int

    fun setSearchEditTextVisibility(value: Int)

    @Bindable
    fun getSearchCloseBtnVisibility(): Int

    fun setSearchCloseBtnVisibility(value: Int)
}