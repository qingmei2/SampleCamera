@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.qingmei2.samplecamera.core.RxCamera
import com.qingmei2.samplecamera.core.impl.Camera1
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

@SuppressWarnings("checkResult")
class Camera1Activity : AppCompatActivity() {

    private var mCameraView: Camera1 by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        if (cameraHardwareAvailable()) {
            RxCamera
                    .build(this, flContainer) {
                        autoFocus = true
                        useBackCamera = true
                    }
                    .openCamera()
                    .subscribe({ result: Boolean ->
                        println("openCamera() successfully: $result")
                    }, {
                        println("openCamera() failed: ${it.message}")
                        it.printStackTrace()
                    })
        }

        btnSwitch.setOnClickListener {
            mCameraView.switchCameraFace()
        }

        btnCapture.setOnClickListener {

        }
    }

    /**
     * 检查相机是否可用
     */
    private fun cameraHardwareAvailable() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

    companion object {
        const val TAG = "Camera1Activity"
    }
}