@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera.utils

import android.annotation.TargetApi
import android.hardware.Camera
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.view.Surface

object CameraUtils {

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun setCameraDisplayOrientation(activity: FragmentActivity,
                                    cameraId: Int = 0,
                                    camera: Camera) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

    /**
     * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
     *
     * @param surfaceWidth 需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList 需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    fun getCloselyPreSize(surfaceWidth: Int,
                          surfaceHeight: Int,
                          preSizeList: List<Camera.Size>,
                          isPortrait: Boolean = true): Camera.Size {

        val reqTmpWidth: Int
        val reqTmpHeight: Int
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight
            reqTmpHeight = surfaceWidth
        } else {
            reqTmpWidth = surfaceWidth
            reqTmpHeight = surfaceHeight
        }
        // 先查找preview中是否存在与SurfaceView相同宽高的尺寸
        for (size in preSizeList) {
            if (size.width == reqTmpWidth && size.height == reqTmpHeight) {
                return size
            }
        }

        // 得到与传入的宽高比最接近的size
        val reqRatio = reqTmpWidth.toFloat() / reqTmpHeight
        var curRatio: Float
        var deltaRatio: Float
        var deltaRatioMin = java.lang.Float.MAX_VALUE
        var retSize: Camera.Size? = null
        for (size in preSizeList) {
            curRatio = size.width.toFloat() / size.height
            deltaRatio = Math.abs(reqRatio - curRatio)
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio
                retSize = size
            }
        }
        return retSize!!
    }
}