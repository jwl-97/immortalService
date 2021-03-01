package com.ljw.immortal_service

import android.content.Context
import android.content.SharedPreferences

class PreferenceUnit private constructor() {
    var preIsRealStop: Boolean
        get() = mPref.getBoolean(PREF_STOP, false)
        set(isRealStop) {
            val edit = mPref.edit()
            edit.putBoolean(PREF_STOP, isRealStop)
            edit.apply()
        }

    companion object {
        private lateinit var mPref: SharedPreferences
        private var mInstance: PreferenceUnit? = null

        private const val PREF_NAME = "rendriverspref"
        private const val PREF_STOP = "rendrivers_stop_service"

        fun init(context: Context) {
            mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        fun getInstance(): PreferenceUnit {
            if (mInstance == null) {
                mInstance = PreferenceUnit()
            }
            return mInstance as PreferenceUnit
        }
    }
}
