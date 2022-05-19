package com.sms.moLotus.socket

import android.content.Context
import com.sms.moLotus.feature.Constants
import io.socket.client.IO
import io.socket.client.Socket

class SocketManager private constructor(private var context: Context) {

    private var socket: Socket? = null

    companion object {
        var instance: SocketManager? = null
        fun getInstance(context: Context): SocketManager? {
            if (instance == null) {
                instance = SocketManager(context)
            }
            return instance
        }
    }


    fun getSocket() = socket

    init {
        socket = IO.socket(Constants.SOCKET_URL)
        socket!!.connect()
    }


}