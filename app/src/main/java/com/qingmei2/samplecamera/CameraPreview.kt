@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

@SuppressLint("ViewConstructor")
class CameraPreview(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    var mCamera: Camera? = null

    private var faceInfo = CAMERA_FACE_BACKGROUND

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        stopPreviewAndFreeCamera()

        mCamera = Camera.open(CAMERA_FACE_BACKGROUND)

        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopPreviewAndFreeCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mCamera?.apply {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            val size = parameters.supportedPreviewSizes[0]

            parameters?.also { params ->
                params.setPreviewSize(size.width, size.height)
                requestLayout()
                parameters = params
            }

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            startPreview()
        }
    }

    private fun getCameraInstance(camera: Int = CAMERA_FACE_BACKGROUND): Camera = Camera.open()

    fun switchCameraFace() {
        stopPreviewAndFreeCamera()

        when (faceInfo) {
            CAMERA_FACE_BACKGROUND ->
                faceInfo = CAMERA_FACE_FOREGROUND
            CAMERA_FACE_FOREGROUND ->
                faceInfo = CAMERA_FACE_BACKGROUND
        }

        mCamera = getCameraInstance(faceInfo)

        startPreview()
    }

    private fun startPreview() {
        try {
            mCamera?.setPreviewDisplay(holder)
            mCamera?.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting mCamera preview: ${e.message}")
        }
    }

    private fun stopPreviewAndFreeCamera() {
        mCamera?.apply {
            // Call stopPreview() to stop updating the preview surface.
            stopPreview()

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            release()

            mCamera = null
        }
    }

    companion object {
        const val TAG = "CameraPreview"

        const val CAMERA_FACE_BACKGROUND = 0
        const val CAMERA_FACE_FOREGROUND = 1
    }
}