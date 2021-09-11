package com.sms.moLotus.feature.gallery

import com.sms.moLotus.model.MmsPart
import io.realm.RealmResults

data class GalleryState(
    val navigationVisible: Boolean = true,
    val title: String? = "",
    val parts: RealmResults<MmsPart>? = null
)
