package marabillas.loremar.beedio.base.mvvm

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel

abstract class ObservableViewModel : ViewModel(), Observable {

    @delegate:Transient
    private val mCallBacks: PropertyChangeRegistry by lazy { PropertyChangeRegistry() }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        mCallBacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        mCallBacks.remove(callback)
    }

    fun notifyChange() {
        mCallBacks.notifyChange(this, 0)
    }

    fun notifyChange(viewId: Int) {
        mCallBacks.notifyChange(this, viewId)
    }
}