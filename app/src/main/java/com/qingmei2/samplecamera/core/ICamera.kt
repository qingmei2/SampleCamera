package com.qingmei2.samplecamera.core

import io.reactivex.Single

interface ICamera {

    fun openCamera(): Single<Boolean>

    fun closeCamera(): Single<Boolean>

    fun switchFlashMode(): Single<Boolean>

    fun switchFaceMode(): Single<Boolean>
}