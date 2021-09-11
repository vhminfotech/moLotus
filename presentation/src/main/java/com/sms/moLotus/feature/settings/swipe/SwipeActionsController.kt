package com.sms.moLotus.feature.settings.swipe

import android.view.View
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.sms.moLotus.R
import com.sms.moLotus.common.QkDialog
import com.sms.moLotus.common.base.QkController
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.animateLayoutChanges
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.common.util.extensions.setTint
import com.sms.moLotus.injection.appComponent
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.swipe_actions_controller.*
import javax.inject.Inject

class SwipeActionsController : QkController<SwipeActionsView, SwipeActionsState, SwipeActionsPresenter>(), SwipeActionsView {

    @Inject override lateinit var presenter: SwipeActionsPresenter
    @Inject lateinit var actionsDialog: QkDialog
    @Inject lateinit var colors: Colors

    /**
     * Allows us to subscribe to [actionClicks] more than once
     */
    private val actionClicks: Subject<SwipeActionsView.Action> = PublishSubject.create()

    init {
        appComponent.inject(this)
        layoutRes = R.layout.swipe_actions_controller

        actionsDialog.adapter.setData(R.array.settings_swipe_actions)
    }

    override fun onViewCreated() {
        colors.theme().let { theme ->
            rightIcon.setBackgroundTint(theme.theme)
            rightIcon.setTint(theme.textPrimary)
            leftIcon.setBackgroundTint(theme.theme)
            leftIcon.setTint(theme.textPrimary)
        }

        right.postDelayed({ right?.animateLayoutChanges = true }, 100)
        left.postDelayed({ left?.animateLayoutChanges = true }, 100)

        Observable.merge(
                right.clicks().map { SwipeActionsView.Action.RIGHT },
                left.clicks().map { SwipeActionsView.Action.LEFT })
                .autoDisposable(scope())
                .subscribe(actionClicks)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.settings_swipe_actions)
        showBackButton(true)
    }

    override fun actionClicks(): Observable<SwipeActionsView.Action> = actionClicks

    override fun actionSelected(): Observable<Int> = actionsDialog.adapter.menuItemClicks

    override fun showSwipeActions(selected: Int) {
        actionsDialog.adapter.selectedItem = selected
        activity?.let(actionsDialog::show)
    }

    override fun render(state: SwipeActionsState) {
        rightIcon.isVisible = state.rightIcon != 0
        rightIcon.setImageResource(state.rightIcon)
        rightLabel.text = state.rightLabel

        leftIcon.isVisible = state.leftIcon != 0
        leftIcon.setImageResource(state.leftIcon)
        leftLabel.text = state.leftLabel
    }

}