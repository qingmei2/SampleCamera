@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

@SuppressWarnings("checkResult")
class Camera1Activity : AppCompatActivity() {

    private var mCameraView: RxCameraView by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        if (cameraHardwareAvailable()) {
            mCameraView = RxCameraView(this).apply {
                keepScreenOn = true
                flContainer.addView(this)
            }
            mCameraView.openCameraObservable()
                    .subscribe { value ->
                        Log.d(TAG, "onNext: $value")
                    }
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