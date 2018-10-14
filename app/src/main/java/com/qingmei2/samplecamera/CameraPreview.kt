@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.qingmei2.samplecamera.utils.CameraUtils
import java.io.IOException

@SuppressLint("ViewConstructor")
class CameraPreview(private val mContext: FragmentActivity) : SurfaceView(mContext), SurfaceHolder.Callback {

    private var mCamera: Camera? = null

    private var mCameraId = CAMERA_FACE_BACKGROUND

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
            val sizes = parameters.supportedPreviewSizes

            parameters?.also { params ->
                val closeSize = CameraUtils.getCloselyPreSize(width, height, sizes)

                params.setPreviewSize(closeSize.width, closeSize.height)
                requestLayout()
                parameters = params
            }

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            startPreview()
        }
    }

    private fun getCameraInstance(cameraId: Int = CAMERA_FACE_BACKGROUND): Camera = Camera.open(cameraId)

    fun switchCameraFace() {
        stopPreviewAndFreeCamera()

        when (mCameraId) {
            CAMERA_FACE_BACKGROUND ->
                mCameraId = CAMERA_FACE_FOREGROUND
            CAMERA_FACE_FOREGROUND ->
                mCameraId = CAMERA_FACE_BACKGROUND
        }

        mCamera = getCameraInstance(mCameraId)

        startPreview()
    }

    private fun startPreview() {
        try {
            mCamera?.apply {
                CameraUtils.setCameraDisplayOrientation(mContext, mCameraId, this)
                setPreviewDisplay(holder)
                startPreview()
            }
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