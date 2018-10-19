package com.qingmei2.samplecamera.core

import android.support.annotation.IntDef
import android.support.v4.app.FragmentActivity
import android.view.ViewGroup
import com.qingmei2.samplecamera.core.camera.Camera1
import com.qingmei2.samplecamera.core.preview.SurfaceCameraPreview
import com.qingmei2.samplecamera.entity.AspectRatio

class RxCamera private constructor(private val mCameraImpl: IRxCamera) : IRxCamera by mCameraImpl {

    companion object {

        fun build(context: FragmentActivity,
                  parent: ViewGroup,
                  supplier: Builder.() -> Unit): RxCamera {
            return Builder(context, parent, supplier).build()
        }

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

    class Builder private constructor(val context: FragmentActivity,
                                      val parent: ViewGroup) {
        var autoFocus: Boolean = true

        var useBackCamera: Boolean = true

        var bindLifecycle: Boolean = false

        constructor(context: FragmentActivity,
                    parent: ViewGroup,
                    supplier: Builder.() -> Unit) : this(context, parent) {
            supplier()
        }

        fun build(): RxCamera {
            val mCameraImpl = Camera1(context, SurfaceCameraPreview(context, parent))
            return RxCamera(mCameraImpl)
        }
    }
}

/** Direction the camera faces relative to device screen.  */
@IntDef(RxCamera.FACING_BACK,
        RxCamera.FACING_FRONT)
annotation class Facing

/** The mode for for the camera device's flash control  */
@IntDef(RxCamera.FLASH_OFF,
        RxCamera.FLASH_ON,
        RxCamera.FLASH_TORCH,
        RxCamera.FLASH_AUTO,
        RxCamera.FLASH_RED_EYE)
annotation class Flash
