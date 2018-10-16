@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.os.Build
import android.support.annotation.IntDef
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewCompat
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.qingmei2.samplecamera.data.AspectRatio
import com.qingmei2.samplecamera.data.Size
import com.qingmei2.samplecamera.data.SizeMap
import com.qingmei2.samplecamera.utils.CameraUtils
import com.qingmei2.samplecamera.utils.DisplayOrientationDetector
import java.io.IOException
import java.util.*

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

    private var mCameraParameters: Camera.Parameters? = null

    private var mCameraInfo = Camera.CameraInfo()

    @Facing
    private var mCameraId: Int = FACING_BACK
    @Flash
    private var mFlashMode: Int = FLASH_OFF

    private var mAspectRatio: AspectRatio = DEFAULT_ASPECT_RATIO

    private val mPreviewSizes = SizeMap()

    private val mPictureSizes = SizeMap()

    private val mDisplayOrientationDetector = object : DisplayOrientationDetector(context) {

        override fun onDisplayOrientationChanged(displayOrientation: Int) {
            setDisplayOrientation(displayOrientation)
        }
    }

    private var autoFocus: Boolean = true
        set(value) {
            field = value
            setAutoFocusInternal(field)
        }

    private var mShowingPreview = false

    private var mDisplayOrientation: Int = 0

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
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

    override fun onAttachedToWindow() {
        mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this)!!)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        mDisplayOrientationDetector.disable()
        super.onDetachedFromWindow()
    }

    private fun initCamera(autoFocusPairProvider: () -> Pair<Boolean, Boolean> = { Pair(true, true) }) {
        stopPreviewAndFreeCamera()

        selectCamera()
        mCamera = Camera.open(mCameraId)

        if (autoFocusPairProvider().first)
            autoFocus = autoFocusPairProvider().second

        mCameraParameters = mCamera?.parameters
        mPreviewSizes.clear()
        for (size in mCameraParameters!!.supportedPreviewSizes) {
            mPreviewSizes.add(Size(size.width, size.height))
        }
        // Supported picture sizes;
        mPictureSizes.clear()
        for (size in mCameraParameters!!.supportedPreviewSizes) {
            mPictureSizes.add(Size(size.width, size.height))
        }
        adjustCameraParameters()
        mCamera?.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation))
    }

    private fun selectCamera() {
        val count = Camera.getNumberOfCameras()
        for (i in 0..count) {
            Camera.getCameraInfo(i, mCameraInfo)
            if (mCameraInfo.facing == mCameraId) {
                mCameraId = i
                return
            }
        }
        mCameraId = -1
    }

    fun switchCameraFace() {
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
                mShowingPreview = true
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
            mShowingPreview = false
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

    fun setAspectRatio(ratio: AspectRatio): Boolean {
        if (mCamera == null) {
            // Handle this later when camera is opened
            mAspectRatio = ratio
            return true
        } else if (mAspectRatio != ratio) {
            val sizes = mPreviewSizes.sizes(ratio)
            if (sizes == null) {
                throw UnsupportedOperationException(ratio.toString() + " is not supported")
            } else {
                mAspectRatio = ratio
                adjustCameraParameters()
                return true
            }
        }
        return false
    }

    private fun adjustCameraParameters() {
        var sizes = mPreviewSizes.sizes(mAspectRatio)
        if (sizes == null) { // Not supported
            mAspectRatio = chooseAspectRatio()
            sizes = mPreviewSizes.sizes(mAspectRatio)
        }
        val size = chooseOptimalSize(sizes!!)

        // Always re-apply camera parameters
        // Largest picture size in this ratio
        val pictureSize = mPictureSizes.sizes(mAspectRatio)!!.last()
        if (mShowingPreview) {
            mCamera?.stopPreview()
        }
        mCameraParameters?.setPreviewSize(size.width, size.height)
        mCameraParameters?.setPictureSize(pictureSize.width, pictureSize.height)
        mCameraParameters?.setRotation(calcCameraRotation(mDisplayOrientation))
        setAutoFocusInternal(autoFocus)
//        setFlashInternal(mFlashMode)
        mCamera?.parameters = mCameraParameters
        if (mShowingPreview) {
            mCamera?.startPreview()
        }
    }

    private fun chooseAspectRatio(): AspectRatio {
        var ratio: AspectRatio? = null
        mPreviewSizes.ratios().forEach {
            if (it == DEFAULT_ASPECT_RATIO) {
                return it
            }
            ratio = it
        }
        return ratio ?: throw NullPointerException("chooseAspectRatio() result is null.")
    }

    private fun chooseOptimalSize(sizes: SortedSet<Size>): Size {
        if (width != 0 && height != 0) { // Not yet laid out
            return sizes.first() // Return the smallest size
        }
        val desiredWidth: Int
        val desiredHeight: Int
        val surfaceWidth = width
        val surfaceHeight = height
        if (isLandscape(mDisplayOrientation)) {
            desiredWidth = surfaceHeight
            desiredHeight = surfaceWidth
        } else {
            desiredWidth = surfaceWidth
            desiredHeight = surfaceHeight
        }
        var result: Size? = null
        for (size in sizes) { // Iterate from small to large
            if (desiredWidth <= size.width && desiredHeight <= size.height) {
                return size
            }
            result = size
        }
        return result!!
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setDisplayOrientation(displayOrientation: Int) {
        if (mDisplayOrientation == displayOrientation) {
            return
        }
        mDisplayOrientation = displayOrientation
        if (mCamera != null) {
            mCameraParameters?.setRotation(calcCameraRotation(displayOrientation))
            mCamera?.parameters = mCameraParameters
            val needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14
            if (needsToStopPreview) {
                mCamera?.stopPreview()
            }
            mCamera?.setDisplayOrientation(calcDisplayOrientation(displayOrientation))
            if (needsToStopPreview) {
                mCamera?.startPreview()
            }
        }
    }


    /**
     * Calculate display orientation
     * https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     *
     *
     * This calculation is used for orienting the preview
     *
     *
     * Note: This is not the same calculation as the camera rotation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees required to rotate preview
     */
    private fun calcDisplayOrientation(screenOrientationDegrees: Int): Int =
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360
            } else {  // back-facing
                (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360
            }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    private fun isLandscape(orientationDegrees: Int): Boolean =
            orientationDegrees == 90 || orientationDegrees == 270

    /**
     * Calculate camera rotation
     *
     *
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     *
     *
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    private fun calcCameraRotation(screenOrientationDegrees: Int): Int {
        return if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (mCameraInfo.orientation + screenOrientationDegrees) % 360
        } else {  // back-facing
            val landscapeFlip = if (isLandscape(screenOrientationDegrees)) 180 else 0
            (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360
        }
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

        val DEFAULT_ASPECT_RATIO = AspectRatio.of(4, 3)
    }
}