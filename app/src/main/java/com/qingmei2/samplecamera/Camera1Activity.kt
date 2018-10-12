@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

class Camera1Activity : AppCompatActivity() {

    var camera: Camera by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        if (cameraHardwareAvailable()) {
            camera = getCameraInstance()
            CameraPreview(this, camera).also {
                cameraPreview.addView(it)
            }
        }
    }

    /**
     * 检查相机是否可用
     */
    private fun cameraHardwareAvailable() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

    private fun getCameraInstance(): Camera = Camera.open()
}