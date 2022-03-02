package com.sms.moLotus

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {

    private const val prefName = "MCHAT"

    private fun initializePref(context: Context): SharedPreferences {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    fun setPreference(context: Context, key: String?, value: Boolean) {
        val editor = initializePref(context).edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getPreference(context: Context, key: String?): Boolean {
        return initializePref(context).getBoolean(key, false)
    }

    fun setStringPreference(context: Context, key: String?, value: String) {
        val editor = initializePref(context).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringPreference(context: Context, key: String?): String? {
        return initializePref(context).getString(key, "")
    }


    fun deletePreference(context: Context, key: String?) {
        initializePref(context).edit().remove(key).commit()
    }
}