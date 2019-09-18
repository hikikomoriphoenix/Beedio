/*
 *     Beedio is an Android app for downloading videos
 *     Copyright (C) 2019 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.beedio.home

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import marabillas.loremar.beedio.sharedui.OnTransitionEndListener
import marabillas.loremar.beedio.sharedui.hideSofKeyboard
import marabillas.loremar.beedio.sharedui.showSoftKeyboard
import javax.inject.Inject
import kotlin.math.roundToInt


class SearchWidgetControllerFragment @Inject constructor() : Fragment(), OnSearchWidgetInteractionListener {

    var searchWidget: SearchWidget? = null

    private val searchWidgetTransition = TransitionSet()
    private val collapseOnActivateEnd = OnTransitionEndListener(this::riseSearchWidget)
    private val collapseOnDeactivateEnd = OnTransitionEndListener(this::fallSearchWidget)
    private val riseEnd = OnTransitionEndListener(this::expandOnActivateSearchWidget)
    private val fallEnd = OnTransitionEndListener(this::expandOnDeactivateSearchWidget)
    private val expandOnActivateEnd = OnTransitionEndListener(this::enableSearchInput)
    private val expandOnDeactivateEnd = OnTransitionEndListener(this::disableSearchInput)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        searchWidgetTransition
                .addTransition(ChangeBounds())
                .addTransition(Slide(Gravity.TOP))
                .duration = 100
    }

    override fun onSearchWidgetClick() {
        activateSearchWidget()
    }

    override fun onSEarchCloseClick() {
        searchWidget?.let {
            if (it.getEditText().text.isNotEmpty()) {
                it.getEditText().text.clear()
            } else {
                deactivateSearchWidget()
            }
        }
    }

    private fun activateSearchWidget() {
        searchWidget?.let {
            if (it.getEditText().visibility != View.VISIBLE) {
                collapseOnActivateSearchWidget()
            }
        }
    }

    private fun deactivateSearchWidget() {
        val activity = activity
        if (activity is Activity) {
            hideSofKeyboard(activity)
            collapseOnDeactivateSearchWidget()
        }
    }

    private fun collapseOnActivateSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.addListener(collapseOnActivateEnd)
            val params = initSearchWidgetTransition(it.getView())
            params.width = (56 * resources.displayMetrics.density).roundToInt()
            it.getView().layoutParams = params
            val homeAppBar = activity?.findViewById<View>(R.id.home_appbar)
            homeAppBar?.visibility = View.GONE
        }
    }

    private fun collapseOnDeactivateSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.addListener(collapseOnDeactivateEnd)
            val params = initSearchWidgetTransition(it.getView())
            params.width = (56 * resources.displayMetrics.density).roundToInt()
            it.getView().layoutParams = params
        }
    }

    private fun riseSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(collapseOnActivateEnd)
            searchWidgetTransition.addListener(riseEnd)
            val params = initSearchWidgetTransition(it.getView()) as ConstraintLayout.LayoutParams
            params.verticalBias = 0f
            it.getView().layoutParams = params
        }
    }

    private fun fallSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(collapseOnDeactivateEnd)
            searchWidgetTransition.addListener(fallEnd)
            val params = initSearchWidgetTransition(it.getView()) as ConstraintLayout.LayoutParams
            params.verticalBias = 0.4f
            it.getView().layoutParams = params
        }
    }

    private fun expandOnActivateSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(riseEnd)
            searchWidgetTransition.addListener(expandOnActivateEnd)
            val params = initSearchWidgetTransition(it.getView())
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            it.getView().layoutParams = params
        }

    }

    private fun expandOnDeactivateSearchWidget() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(fallEnd)
            searchWidgetTransition.addListener(expandOnDeactivateEnd)
            val params = initSearchWidgetTransition(it.getView())
            params.width = (304 * resources.displayMetrics.density).roundToInt()
            it.getView().layoutParams = params

            val homeAppBar = activity?.findViewById<View>(R.id.home_appbar)
            homeAppBar?.visibility = View.VISIBLE
        }

    }

    private fun enableSearchInput() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(expandOnActivateEnd)

            it.getSearchIcon().visibility = View.GONE
            it.getCloseButton().visibility = View.VISIBLE
            it.getEditText().visibility = View.VISIBLE
            it.getEditText().requestFocus()

            val activity = activity
            if (activity is Activity)
                showSoftKeyboard(activity)
        }
    }

    private fun disableSearchInput() {
        searchWidget?.let {
            searchWidgetTransition.removeListener(expandOnDeactivateEnd)

            it.getCloseButton().visibility = View.GONE
            it.getSearchIcon().visibility = View.VISIBLE
            it.getEditText().visibility = View.GONE
        }
    }

    private fun initSearchWidgetTransition(searchWidgetView: View): ViewGroup.LayoutParams {
        activity?.let {
            TransitionManager.beginDelayedTransition(
                    it.findViewById(android.R.id.content),
                    searchWidgetTransition)
        }

        return searchWidgetView.layoutParams
    }
}