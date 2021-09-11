package com.sms.moLotus.feature.backup

import com.sms.moLotus.common.base.QkViewContract
import com.sms.moLotus.model.BackupFile
import io.reactivex.Observable

interface BackupView : QkViewContract<BackupState> {

    fun activityVisible(): Observable<*>
    fun restoreClicks(): Observable<*>
    fun restoreFileSelected(): Observable<BackupFile>
    fun restoreConfirmed(): Observable<*>
    fun stopRestoreClicks(): Observable<*>
    fun stopRestoreConfirmed(): Observable<*>
    fun fabClicks(): Observable<*>

    fun requestStoragePermission()
    fun selectFile()
    fun confirmRestore()
    fun stopRestore()

}