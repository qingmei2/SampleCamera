@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.support.annotation.IntDef
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.qingmei2.samplecamera.utils.CameraUtils
import java.io.IOException

@SuppressLint("ViewConstructor")
class CameraPreview(private val mContext: FragmentActivity) : SurfaceView(mContext), SurfaceHolder.Callback {

    /** Direction the camera faces relative to device screen.  */
    @IntDef(FACING_BACK,
            FACING_FRONT)
    annotation class Facing

    /** The mode for for the camera device's flash control  */
    @IntDef(FLASH_OFF,
            FLASH_ON,
            FLASH_TORCH,
            FLASH_AUTO,
            FLASH_RED_EYE)
    annotation class Flash

    private var mCamera: Camera? = null

    @Facing
    private var mCameraId: Int = FACING_BACK
    @Flash
    private var mFlashMode: Int = FLASH_OFF

    var autoFocus: Boolean = false
        set(value) {
            if (field == value)
                return
            field = value
            setAutoFocusInternal(value)
        }

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        stopPreviewAndFreeCamera()

        initCamera()

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

            startPreview()
        }
    }

    private fun initCamera(autoFocusMode: Boolean = true) {
        mCamera = Camera.open(mCameraId)

        if (autoFocusMode != autoFocus)
            autoFocus = autoFocusMode
    }

    fun switchCameraFace() {
        stopPreviewAndFreeCamera()

        when (mCameraId) {
            FACING_BACK ->
                mCameraId = FACING_FRONT
            FACING_FRONT ->
                mCameraId = FACING_BACK
        }

        initCamera()

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

            release()

            mCamera = null
        }
    }

    private fun setAutoFocusInternal(autoFocus: Boolean) {
        val parameters = mCamera?.parameters?.apply {
            focusMode = if (autoFocus && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                Camera.Parameters.FOCUS_MODE_FIXED
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                Camera.Parameters.FOCUS_MODE_INFINITY
            } else {
                supportedFocusModes[0]
            }
        }
        mCamera?.parameters = parameters
    }

    companion object {
        const val TAG = "CameraPreview"

        /** The camera device faces the opposite direction as the device's screen.  */
        const val FACING_BACK = 0

        /** The camera device faces the same direction as the device's screen.  */
        const val FACING_FRONT = 1

        /** Flash will not be fired.  */
        const val FLASH_OFF = 0

        /** Flash will always be fired during snapshot.  */
        const val FLASH_ON = 1

        /** Constant emission of light during preview, auto-focus and snapshot.  */
        const val FLASH_TORCH = 2

        /** Flash will be fired automatically when required.  */
        const val FLASH_AUTO = 3

        /** Flash will be fired in red-eye reduction mode.  */
        const val FLASH_RED_EYE = 4
    }
}