package com.sms.moLotus.feature.plus

data class PlusState(
    val upgraded: Boolean = false,
    val upgradePrice: String = "",
    val upgradeDonatePrice: String = "",
    val currency: String = ""
)