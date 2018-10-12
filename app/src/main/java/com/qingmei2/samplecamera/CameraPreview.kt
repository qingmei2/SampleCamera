@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraPreview(context: Context,
                    private val camera: Camera) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: ${e.message}")
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    companion object {
        const val TAG = "CameraPreview"
    }
}