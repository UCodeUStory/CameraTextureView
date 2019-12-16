package com.ustory.cameratextureview


import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import com.ustory.cameratextureview.CameraUtils.sizeComparator

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class CameraTextureView(context: Context, attrs: AttributeSet) :
    TextureView(context, attrs), View.OnLayoutChangeListener {
    var mCamera: Camera? = null
    private var isCanTakePicture = false
    internal var matrix: Matrix? = null
    internal var mWidth = 0
    internal var mHeight = 0
    private var preferredWidth = 1280
    private var preferredHeight = 720
    var parameters: Camera.Parameters? = null
    internal var orientation = 0
    var takePictureCallBack: ((ByteArray, callBack: (Bitmap?) -> Unit) -> Unit)? = null

    internal var mPictureCallback: Camera.PictureCallback = Camera.PictureCallback { data, camera ->
        mCamera?.let {
            it.stopPreview()
            takePictureCallBack?.invoke(data) { bitmapData ->
                this.post {
                    this@CameraTextureView.setBackgroundDrawable(BitmapDrawable(bitmapData))
                }
            }
        }
    }

    init {
        init()
    }

    private fun init() {
        if (null == mCamera) {
            Log.i("tag","open camera")
            mCamera = Camera.open()
            parameters = mCamera?.parameters
            initParams()
        }
        this.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.i("tag", "onSurfaceTextureAvailable")
                setCameraParams(surfaceTexture)

            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                Log.i("tag", "onSurfaceTextureDestroyed")
                mCamera?.let {
                    it.stopPreview()
                    it.release()
                    mCamera = null
                    isCanTakePicture = true
                }
                return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

            }
        }
    }



    private fun setCameraParams(surfaceTexture: SurfaceTexture) {
        mCamera?.let {
            try {
                it.setPreviewTexture(surfaceTexture)
                it.startPreview()
                isCanTakePicture = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    fun startPreview() {
        if (mCamera != null && !isCanTakePicture) {
            this@CameraTextureView.setBackgroundDrawable(null)
            mCamera!!.startPreview()
            isCanTakePicture = true
        }
    }

    fun stopPreview() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
        }
    }

    fun releaseTextureView() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
            isCanTakePicture = true
        }
    }


    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        mWidth = right - left
        mHeight = bottom - top
    }


    companion object {
        const val TAG = "CameraTextureView"
    }

}

