@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

class Camera1Activity : AppCompatActivity() {

    private var mCameraPreview: CameraPreview by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        if (cameraHardwareAvailable()) {
            mCameraPreview = CameraPreview(this).apply {
                keepScreenOn = true
                flContainer.addView(this)
            }
        }

        btnSwitch.setOnClickListener {
            mCameraPreview.switchCameraFace()
        }
        btnCapture.setOnClickListener {

        }
    }

    /**
     * 检查相机是否可用
     */
    private fun cameraHardwareAvailable() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}