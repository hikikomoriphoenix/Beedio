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

    private val searchWidgetTransition = TransitionSet()
    private val collapseOnActivateEnd = OnTransitionEndListener(this::riseSearchWidget)
    private val collapseOnDeactivateEnd = OnTransitionEndListener(this::fallSearchWidget)
    private val riseEnd = OnTransitionEndListener(this::expandOnActivateSearchWidget)
    private val fallEnd = OnTransitionEndListener(this::expandOnDeactivateSearchWidget)
    private val expandOnActivateEnd = OnTransitionEndListener(this::enableSearchInput)
    private val expandOnDeactivateEnd = OnTransitionEndListener(this::disableSearchInput)

    lateinit var activity: HomeActivity
    lateinit var searchWidget: View

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as HomeActivity
        searchWidget = activity.binding.mainContentHome.homeSearchWidget.root

        searchWidgetTransition
                .addTransition(ChangeBounds())
                .addTransition(Slide(Gravity.TOP))
                .duration = 100
    }

    override fun onSearchWidgetClick() {
        activateSearchWidget()
    }

    override fun onSEarchCloseClick() {
        val editText = activity.binding.mainContentHome.homeSearchWidget.homeSearchEditText
        if (editText.text.isNotEmpty()) {
            editText.text.clear()
        } else {
            deactivateSearchWidget()
        }
    }

    private fun activateSearchWidget() {
        if (activity.binding.mainContentHome.homeSearchWidget.homeSearchEditText.visibility != View.VISIBLE) {
            collapseOnActivateSearchWidget()
        }
    }

    private fun deactivateSearchWidget() {
        hideSofKeyboard(activity)
        collapseOnDeactivateSearchWidget()
    }

    private fun collapseOnActivateSearchWidget() {
        searchWidgetTransition.addListener(collapseOnActivateEnd)
        val params = initSearchWidgetTransition()
        params.width = (56 * resources.displayMetrics.density).roundToInt()
        searchWidget.layoutParams = params
        activity.binding.mainContentHome.homeAppbar.visibility = View.GONE
    }

    private fun collapseOnDeactivateSearchWidget() {
        searchWidgetTransition.addListener(collapseOnDeactivateEnd)
        val params = initSearchWidgetTransition()
        params.width = (56 * resources.displayMetrics.density).roundToInt()
        searchWidget.layoutParams = params
    }

    private fun riseSearchWidget() {
        searchWidgetTransition.removeListener(collapseOnActivateEnd)
        searchWidgetTransition.addListener(riseEnd)
        val params = initSearchWidgetTransition() as ConstraintLayout.LayoutParams
        params.verticalBias = 0f
        searchWidget.layoutParams = params
    }

    private fun fallSearchWidget() {
        searchWidgetTransition.removeListener(collapseOnDeactivateEnd)
        searchWidgetTransition.addListener(fallEnd)
        val params = initSearchWidgetTransition() as ConstraintLayout.LayoutParams
        params.verticalBias = 0.4f
        searchWidget.layoutParams = params
    }

    private fun expandOnActivateSearchWidget() {
        searchWidgetTransition.removeListener(riseEnd)
        searchWidgetTransition.addListener(expandOnActivateEnd)
        val params = initSearchWidgetTransition()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        searchWidget.layoutParams = params
    }

    private fun expandOnDeactivateSearchWidget() {
        searchWidgetTransition.removeListener(fallEnd)
        searchWidgetTransition.addListener(expandOnDeactivateEnd)
        val params = initSearchWidgetTransition()
        params.width = (304 * activity.resources.displayMetrics.density).roundToInt()
        searchWidget.layoutParams = params
        activity.binding.mainContentHome.homeAppbar.visibility = View.VISIBLE
    }

    private fun enableSearchInput() {
        searchWidgetTransition.removeListener(expandOnActivateEnd)

        activity.binding.mainContentHome.homeSearchWidget.homeSearchIcon.visibility = View.GONE
        activity.binding.mainContentHome.homeSearchWidget.homeSearchCloseBtn.visibility = View.VISIBLE

        val editText = activity.binding.mainContentHome.homeSearchWidget.homeSearchEditText
        editText.visibility = View.VISIBLE
        editText.requestFocus()

        showSoftKeyboard(activity)
    }

    private fun disableSearchInput() {
        searchWidgetTransition.removeListener(expandOnDeactivateEnd)

        activity.binding.mainContentHome.homeSearchWidget.homeSearchCloseBtn.visibility = View.GONE
        activity.binding.mainContentHome.homeSearchWidget.homeSearchIcon.visibility = View.VISIBLE
        activity.binding.mainContentHome.homeSearchWidget.homeSearchEditText.visibility = View.GONE
    }

    private fun initSearchWidgetTransition(): ViewGroup.LayoutParams {
        TransitionManager.beginDelayedTransition(
                activity.findViewById(android.R.id.content),
                searchWidgetTransition)
        return searchWidget.layoutParams
    }
}