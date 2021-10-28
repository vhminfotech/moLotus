package com.sms.moLotus.listener

import io.reactivex.Observable

interface ContactAddedListener {

    fun listen(): Observable<*>

}
