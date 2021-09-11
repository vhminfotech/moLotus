package com.sms.moLotus.feature.gallery

import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.model.MmsPart
import io.reactivex.Observable

interface GalleryView : QkView<GalleryState> {

    fun optionsItemSelected(): Observable<Int>
    fun screenTouched(): Observable<*>
    fun pageChanged(): Observable<MmsPart>

    fun requestStoragePermission()

}