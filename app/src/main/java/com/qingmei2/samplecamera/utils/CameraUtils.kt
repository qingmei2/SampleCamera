@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera.utils

import android.annotation.TargetApi
import android.app.Activity
import android.hardware.Camera
import android.os.Build
import android.view.Surface


object CameraUtils {

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun setCameraDisplayOrientation(activity: Activity,
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
}