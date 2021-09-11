package com.sms.moLotus.feature.blocking

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.jakewharton.rxbinding2.view.clicks
import com.sms.moLotus.R
import com.sms.moLotus.common.QkChangeHandler
import com.sms.moLotus.common.base.QkController
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.animateLayoutChanges
import com.sms.moLotus.feature.blocking.manager.BlockingManagerController
import com.sms.moLotus.feature.blocking.messages.BlockedMessagesController
import com.sms.moLotus.feature.blocking.numbers.BlockedNumbersController
import com.sms.moLotus.injection.appComponent
import kotlinx.android.synthetic.main.blocking_controller.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class BlockingController : QkController<BlockingView, BlockingState, BlockingPresenter>(), BlockingView {

    override val blockingManagerIntent by lazy { blockingManager.clicks() }
    override val blockedNumbersIntent by lazy { blockedNumbers.clicks() }
    override val blockedMessagesIntent by lazy { blockedMessages.clicks() }
    override val dropClickedIntent by lazy { drop.clicks() }

    @Inject lateinit var colors: Colors
    @Inject override lateinit var presenter: BlockingPresenter

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocking_controller
    }

    override fun onViewCreated() {
        super.onViewCreated()
        parent.postDelayed({ parent?.animateLayoutChanges = true }, 100)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocking_title)
        showBackButton(true)
    }

    override fun render(state: BlockingState) {
        blockingManager.summary = state.blockingManager
        drop.checkbox.isChecked = state.dropEnabled
        blockedMessages.isEnabled = !state.dropEnabled
    }

    override fun openBlockedNumbers() {
        router.pushController(RouterTransaction.with(BlockedNumbersController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockedMessages() {
        router.pushController(RouterTransaction.with(BlockedMessagesController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun openBlockingManager() {
        router.pushController(RouterTransaction.with(BlockingManagerController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}
