package com.qingmei2.samplecamera.core

import io.reactivex.Flowable
import io.reactivex.Single

interface IRxCamera {

    fun openCamera(): Single<Boolean>

    fun closeCamera(): Single<Boolean>

    fun switchFlashMode(): Single<Boolean>

    fun switchFaceMode(): Single<Boolean>

    fun fetchPreviewFlowable(): Flowable<ByteArray>
}