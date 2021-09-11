package com.sms.moLotus.common.util.extensions

import android.content.Context
import com.sms.moLotus.R
import com.sms.moLotus.repository.BackupRepository

fun BackupRepository.Progress.getLabel(context: Context): String? {
    return when (this) {
        is BackupRepository.Progress.Parsing -> context.getString(R.string.backup_progress_parsing)
        is BackupRepository.Progress.Running -> context.getString(R.string.backup_progress_running, count, max)
        is BackupRepository.Progress.Saving -> context.getString(R.string.backup_progress_saving)
        is BackupRepository.Progress.Syncing -> context.getString(R.string.backup_progress_syncing)
        is BackupRepository.Progress.Finished -> context.getString(R.string.backup_progress_finished)
        else -> null
    }
}