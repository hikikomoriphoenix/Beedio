/*
 * Beedio is an Android app for downloading videos
 * Copyright (C) 2019 Loremar Marabillas
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.base.mvvm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.*

class SendLiveData<T> : MutableLiveData<SendLiveData<T>.ValueData>() {

    private val observerStack = Stack<ObserverData>()

    fun send(value: T) {
        setValue(ValueData(value))
    }

    override fun setValue(value: ValueData?) {
        super.setValue(value)

        while (observerStack.isNotEmpty()) {
            observerStack.pop().also {
                super.observe(it.lifecycleOwner, it.observer)
            }
        }
    }

    fun observeSend(owner: LifecycleOwner, observer: Observer<in T>) {
        observe(owner, Observer { observer.onChanged(it.value) })
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in ValueData>) {
        observerStack.push(ObserverData(owner, observer))
    }


    inner class ValueData(val value: T)

    private inner class ObserverData(val lifecycleOwner: LifecycleOwner, val observer: Observer<in ValueData>)
}