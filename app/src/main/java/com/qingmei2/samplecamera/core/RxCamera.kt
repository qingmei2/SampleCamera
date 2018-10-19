package com.qingmei2.samplecamera.core

import android.support.v4.app.FragmentActivity
import android.view.ViewGroup
import com.qingmei2.samplecamera.core.impl.Camera1
import com.qingmei2.samplecamera.core.preview.SurfaceCameraPreview

class RxCamera private constructor(private val mCameraImpl: IRxCamera) : IRxCamera by mCameraImpl {

    companion object {

        fun build(context: FragmentActivity,
                  parent: ViewGroup,
                  supplier: Builder.() -> Unit): RxCamera {
            return Builder(context, parent, supplier).build()
        }
    }

    class Builder private constructor(val context: FragmentActivity,
                                      val parent: ViewGroup) {

        var autoFocus = true

        var useBackCamera = true

        constructor(context: FragmentActivity,
                    parent: ViewGroup,
                    supplier: Builder.() -> Unit) : this(context, parent) {
            supplier()
        }

        fun build(): RxCamera {
            val mCameraImpl = Camera1(context, parent, SurfaceCameraPreview(context, parent))
            return RxCamera(mCameraImpl)
        }
    }
}