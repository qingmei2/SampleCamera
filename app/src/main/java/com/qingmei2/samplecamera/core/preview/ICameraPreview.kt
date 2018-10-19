package com.qingmei2.samplecamera.core.preview

import android.view.Surface
import android.view.View

interface ICameraPreview {

    fun onSurfaceChanged(onChanged: Listener)

    fun onSurfaceCreated(onCreated: Listener)

    fun onSurfaceDestroy(onDestroy: Listener)

    fun getSurface(): Surface

    fun getView(): View

    fun getOutputClass(): Class<*>

    fun isReady(): Boolean

    fun setDisplayOrientation(displayOrientation: Int)

    fun setSize(sizePair: Pair<Int, Int>)

    fun getSize(): Pair<Int, Int>
}

typealias Listener = () -> Unit