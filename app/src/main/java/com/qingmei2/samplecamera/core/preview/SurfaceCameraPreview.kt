package com.qingmei2.samplecamera.core.preview

import android.content.Context
import android.support.v4.view.ViewCompat
import android.view.*
import com.qingmei2.samplecamera.R
import kotlin.properties.Delegates

@Suppress("DEPRECATION")
class SurfaceCameraPreview(context: Context, parent: ViewGroup) : ICameraPreview {

    private var mSurfaceView: SurfaceView by Delegates.notNull()

    private var onChanged: Listener? = null
    private var onCreated: Listener? = null
    private var onDestroy: Listener? = null

    private var sizePair: Pair<Int, Int> = Pair(0, 0)

    init {
        val view = View.inflate(context, R.layout.surface_view, parent)
        mSurfaceView = view.findViewById<View>(R.id.surface_view) as SurfaceView
        mSurfaceView.holder.apply {
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            addCallback(object : SurfaceHolder.Callback {

                override fun surfaceCreated(h: SurfaceHolder) {
                    onCreated?.invoke()
                }

                override fun surfaceChanged(h: SurfaceHolder, format: Int, width: Int, height: Int) {
                    setSize(Pair(width, height))
                    if (!ViewCompat.isInLayout(mSurfaceView)) {
                        onChanged?.invoke()
                    }
                }

                override fun surfaceDestroyed(h: SurfaceHolder) {
                    setSize(Pair(0, 0))
                    onDestroy?.invoke()
                }
            })
        }
    }

    override fun onSurfaceChanged(onChanged: Listener) {
        this.onChanged = onChanged
    }

    override fun onSurfaceCreated(onCreated: Listener) {
        this.onCreated = onCreated
    }

    override fun onSurfaceDestroy(onDestroy: Listener) {
        this.onDestroy = onDestroy
    }

    override fun getSurface(): Surface = mSurfaceView.holder.surface

    override fun getView(): View = mSurfaceView

    override fun getOutputClass(): Class<*> = SurfaceHolder::class.java

    override fun isReady(): Boolean = sizePair.first != 0 && sizePair.second != 0

    override fun setDisplayOrientation(displayOrientation: Int) {

    }

    override fun setSize(sizePair: Pair<Int, Int>) {
        this.sizePair = sizePair
    }

    override fun getSize(): Pair<Int, Int> = sizePair
}