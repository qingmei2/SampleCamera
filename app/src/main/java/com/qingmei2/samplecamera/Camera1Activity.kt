@file:Suppress("DEPRECATION")

package com.qingmei2.samplecamera

import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_camera1.*
import kotlin.properties.Delegates

class Camera1Activity : AppCompatActivity() {

    var mCamera: Camera by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        setSupportActionBar(toolbar)

        if (cameraHardwareAvailable()) {
            CameraPreview(this).also {
                cameraPreview.addView(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_camera1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_switch_camera -> {
//                CameraUtils.setCameraDisplayOrientation(this, 0, mCamera)
//                cameraView.switchCameraFace()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * 检查相机是否可用
     */
    private fun cameraHardwareAvailable() =
            packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}