package com.sms.moLotus.feature.backup

import com.sms.moLotus.model.BackupFile
import com.sms.moLotus.repository.BackupRepository

data class BackupState(
    val backupProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val restoreProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val lastBackup: String = "",
    val backups: List<BackupFile> = listOf(),
    val upgraded: Boolean = false
)