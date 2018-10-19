@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.qingmei2.samplecamera.core.RxCamera
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

@SuppressWarnings("checkResult")
class Camera1Activity : AppCompatActivity() {

    private var mCamera: RxCamera by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        if (cameraHardwareAvailable()) {
            mCamera = RxCamera
                    .build(this, flContainer) {
                        autoFocus = true
                        useBackCamera = true
                    }.apply {
                        openCamera()
                                .subscribe({ result: Boolean ->
                                    Log.d(TAG, "openCamera() successfully: $result")
                                }, {
                                    Log.d(TAG, "openCamera() failed: ${it.message}")
                                    it.printStackTrace()
                                })
                    }
        }

        btnSwitch.setOnClickListener { _ ->
            mCamera.switchFaceMode()
                    .subscribe({ result: Boolean ->
                        Log.d(TAG, "switchFaceMode() successfully: $result")
                    }, {
                        Log.d(TAG, "switchFaceMode() failed: ${it.message}")
                        it.printStackTrace()
                    })
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