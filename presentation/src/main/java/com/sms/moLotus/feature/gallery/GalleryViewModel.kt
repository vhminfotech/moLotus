package com.sms.moLotus.feature.gallery

import android.content.Context
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.base.QkViewModel
import com.sms.moLotus.common.util.extensions.makeToast
import com.sms.moLotus.extensions.mapNotNull
import com.sms.moLotus.interactor.SaveImage
import com.sms.moLotus.manager.PermissionManager
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject
import javax.inject.Named

class GalleryViewModel @Inject constructor(
    conversationRepo: ConversationRepository,
    @Named("partId") private val partId: Long,
    private val context: Context,
    private val messageRepo: MessageRepository,
    private val navigator: Navigator,
    private val saveImage: SaveImage,
    private val permissions: PermissionManager
) : QkViewModel<GalleryView, GalleryState>(GalleryState()) {

    init {
        disposables += Flowable.just(partId)
                .mapNotNull(messageRepo::getMessageForPart)
                .mapNotNull { message -> message.threadId }
                .doOnNext { threadId -> newState { copy(parts = messageRepo.getPartsForConversation(threadId)) } }
                .doOnNext { threadId ->
                    newState {
                        copy(title = conversationRepo.getConversation(threadId)?.getTitle())
                    }
                }
                .subscribe()
    }

    override fun bindView(view: GalleryView) {
        super.bindView(view)

        // When the screen is touched, toggle the visibility of the navigation UI
        view.screenTouched()
                .withLatestFrom(state) { _, state -> state.navigationVisible }
                .map { navigationVisible -> !navigationVisible }
                .autoDisposable(view.scope())
                .subscribe { navigationVisible -> newState { copy(navigationVisible = navigationVisible) } }

        // Save image to device
        view.optionsItemSelected()
                .filter { itemId -> itemId == R.id.save }
                .filter { permissions.hasStorage().also { if (!it) view.requestStoragePermission() } }
                .withLatestFrom(view.pageChanged()) { _, part -> part.id }
                .autoDisposable(view.scope())
                .subscribe { partId -> saveImage.execute(partId) { context.makeToast(R.string.gallery_toast_saved) } }

        // Share image externally
//        view.optionsItemSelected()
//                .filter { itemId -> itemId == R.id.share }
//                .filter { permissions.hasStorage().also { if (!it) view.requestStoragePermission() } }
//                .withLatestFrom(view.pageChanged()) { _, part -> part.id }
//                .autoDisposable(view.scope())
//                .subscribe { partId -> messageRepo.savePart(partId)?.let(navigator::shareFile) }
    }

}
