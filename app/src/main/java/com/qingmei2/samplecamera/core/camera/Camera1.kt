package com.qingmei2.samplecamera.core.camera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewCompat
import android.util.Log
import android.view.SurfaceView
import com.qingmei2.samplecamera.core.Facing
import com.qingmei2.samplecamera.core.Flash
import com.qingmei2.samplecamera.core.IRxCamera
import com.qingmei2.samplecamera.core.RxCamera.Companion.DEFAULT_ASPECT_RATIO
import com.qingmei2.samplecamera.core.RxCamera.Companion.FACING_BACK
import com.qingmei2.samplecamera.core.RxCamera.Companion.FACING_FRONT
import com.qingmei2.samplecamera.core.RxCamera.Companion.FLASH_OFF
import com.qingmei2.samplecamera.core.preview.ICameraPreview
import com.qingmei2.samplecamera.entity.AspectRatio
import com.qingmei2.samplecamera.entity.Size
import com.qingmei2.samplecamera.entity.SizeMap
import com.qingmei2.samplecamera.utils.CameraUtils
import com.qingmei2.samplecamera.utils.DisplayOrientationDetector
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import java.io.IOException

@Suppress("DEPRECATION")
@SuppressWarnings("checkResult", "ViewConstructor")
class Camera1(private val mContext: FragmentActivity,
              private val mCameraPreview: ICameraPreview) : IRxCamera {

    private var openCameraSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var previewProcessor: PublishProcessor<ByteArray> = PublishProcessor.create()

    private var attachProcessor: PublishProcessor<Unit> = PublishProcessor.create()

    private var endProcessor: PublishProcessor<Unit> = PublishProcessor.create()

    private var mCamera: Camera? = null

    private var mCameraInfo = Camera.CameraInfo()

    @Facing
    private var mCameraId: Int = FACING_BACK
    @Flash
    private var mFlashMode: Int = FLASH_OFF

    private var mAspectRatio: AspectRatio = DEFAULT_ASPECT_RATIO

    private val mPreviewSizes = SizeMap()

    private val mPictureSizes = SizeMap()

    private var isCreated = false

    private var mShowingPreview = false

    private var mDisplayOrientation: Int = 0

    private val mDisplayOrientationDetector = object : DisplayOrientationDetector(mContext) {

        override fun onDisplayOrientationChanged(displayOrientation: Int) {
            setDisplayOrientation(displayOrientation)
        }
    }

    override fun openCamera(): Single<Boolean> {
        openCameraAsync()
        return openCameraSubject
                .single(false)
    }

    override fun closeCamera(): Single<Boolean> = Single.create { emitter ->
        stopPreviewAndFreeCamera()
        emitter.onSuccess(true)
    }

    override fun switchFlashMode(): Single<Boolean> = Single.create { emitter ->
        switchCameraFace()
        emitter.onSuccess(true)
    }

    override fun switchFaceMode(): Single<Boolean> = Single.create { emitter ->
        switchCameraFace()
        emitter.onSuccess(true)
    }

    override fun fetchPreviewFlowable(): Flowable<ByteArray> {
        openCameraAsync()
        return previewProcessor.takeUntil(endProcessor)
    }

    private var autoFocus: Boolean = true
        set(value) {
            field = value
            setAutoFocusInternal(field)
        }

    init {
        mCameraPreview.apply {
            onSurfaceChanged {
                isCreated = true
                attachProcessor.onNext(Unit)
                endProcessor = PublishProcessor.create()

                mDisplayOrientationDetector.enable(ViewCompat.getDisplay(mCameraPreview.getView())!!)
            }

            onSurfaceDestroy {
                stopPreviewAndFreeCamera()

                mDisplayOrientationDetector.disable()

                endProcessor.onNext(Unit)
                endProcessor.onComplete()
                attachProcessor.onComplete()
            }
        }
    }

    private fun openCameraAsync() {
        openCameraSubject = PublishSubject.create()

        if (isCreated) {
            openCameraInternal()
        } else {
            attachProcessor.subscribe {
                openCameraInternal()
            }
        }
    }

    private fun openCameraInternal() {
        try {
            initCamera()
            startPreview()
            openCameraSubject.onNext(true)
        } catch (e: Exception) {
            openCameraSubject.onNext(false)
        }
        openCameraSubject.onComplete()
    }

    private fun initCamera(autoFocusPairProvider: () -> Pair<Boolean, Boolean> = { Pair(true, true) }) {
        stopPreviewAndFreeCamera()

        selectCamera()
        mCamera = Camera.open(mCameraId)

        if (autoFocusPairProvider().first)
            autoFocus = autoFocusPairProvider().second

        mCamera!!.parameters.apply {
            mPreviewSizes.clear()
            supportedPreviewSizes.forEach {
                mPreviewSizes.add(Size(it.width, it.height))
            }
            mPictureSizes.clear()
            supportedPictureSizes.forEach {
                mPictureSizes.add(Size(it.width, it.height))
            }
            adjustCameraParameters()
        }

        mCamera?.setDisplayOrientation(calcDisplayOrientation(mDisplayOrientation))
        mCamera?.setPreviewCallback { bytes: ByteArray, _: Camera ->
            previewProcessor.onNext(bytes)
        }
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

    private fun switchCameraFace() {
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

                val surfaceView = mCameraPreview.getView() as SurfaceView
                setPreviewDisplay(surfaceView.holder)
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

    private fun setAspectRatio(ratio: AspectRatio): Boolean {
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
        mCamera?.apply {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            val sizes = parameters.supportedPreviewSizes
            val pictureSize = mPictureSizes.sizes(mAspectRatio)!!.last()

            stopPreview()

            val params = parameters?.apply {
                CameraUtils.getCloselyPreSize(
                        mCameraPreview.getSize().first,
                        mCameraPreview.getSize().second,
                        sizes
                ).also { closeSize ->
                    setPreviewSize(closeSize.width, closeSize.height)
                }

                setPictureSize(pictureSize.width, pictureSize.height)
                setRotation(calcCameraRotation(mDisplayOrientation))
            }

            parameters = params

            setAutoFocusInternal(autoFocus)
            // setFlashInternal(mFlashMode)

            startPreview()
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

    @SuppressLint("ObsoleteSdkInt")
    fun setDisplayOrientation(displayOrientation: Int) {
        if (mDisplayOrientation == displayOrientation) {
            return
        }
        mDisplayOrientation = displayOrientation
        mCamera?.apply {
            parameters.setRotation(calcCameraRotation(displayOrientation))
            val needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14
            if (needsToStopPreview) {
                stopPreview()
            }
            setDisplayOrientation(calcDisplayOrientation(displayOrientation))
            if (needsToStopPreview) {
                startPreview()
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
        const val TAG = "Camera1"
    }
}