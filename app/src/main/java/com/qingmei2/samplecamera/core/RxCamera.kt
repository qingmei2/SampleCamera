package com.qingmei2.samplecamera.core

class RxCamera private constructor(val builder: Builder) {

    companion object {

        fun build(supplier: Builder.() -> Unit): RxCamera {
            return Builder(supplier).build()
        }
    }

    class Builder private constructor() {

        constructor(supplier: Builder.() -> Unit) : this() {
            supplier()
        }

        fun build(): RxCamera {
            return RxCamera(this)
        }
    }
}