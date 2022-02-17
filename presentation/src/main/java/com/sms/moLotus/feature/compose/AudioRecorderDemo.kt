package com.sms.moLotus.feature.compose

import java.io.File;
import java.io.IOException;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log

object AudioRecorderDemo {
    val recorder: MediaRecorder = MediaRecorder()
    var path: String = "AudioRecording"

    /*fun AudioRecorder() {

    }*/

    private fun sanitizePath(): String {
        if (!path.startsWith("/")) {
            path = "/$path"
        }
        if (!path.contains(".")) {
            path += ".3gp"
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath()
            .toString() + path
    }

    @Throws(IOException::class)
    fun start() {
        path = sanitizePath()
        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            throw IOException(
                "SD Card is not mounted.  It is " + state
                        + "."
            )
        }

        // make sure the directory we plan to store the recording in exists
        val directory: File = File(path).parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Path to file could not be created.")
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(path)
        recorder.prepare()
        recorder.start()


        Log.e("AUDIORECORDER","path:::::: $path")
    }

    @Throws(IOException::class)
    fun stop() {
        recorder.stop()
        recorder.release()
    }

    @Throws(IOException::class)
    fun playarcoding(path: String?) {
        val mp = MediaPlayer()
        mp.setDataSource(path)
        mp.prepare()
        mp.start()
        mp.setVolume(10f, 10f)
    }
}