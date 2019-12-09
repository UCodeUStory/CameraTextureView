package com.ustory.cameratextureview

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SurfaceTexture
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


import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class CameraTextureView(context: Context, attrs: AttributeSet) :
    TextureView(context, attrs), View.OnLayoutChangeListener {
    var mCamera: Camera? = null
    private var param: Camera.Parameters? = null
    private var isCanTakePicture = false
    internal var matrix: Matrix? = null
    internal var camera: Camera? = null
    internal var mWidth = 0
    internal var mHeight = 0
    internal var mDisplayWidth = 0
    internal var mDisplayHeight = 0
    internal var mPreviewWidth = 640
    internal var mPreviewHeight = 480
    internal var orientation = 0
    var takePictureCallBack:((ByteArray,callBack:(Bitmap?)->Unit)->Unit)?=null

    internal var mPictureCallback: Camera.PictureCallback = Camera.PictureCallback { data, camera ->
        mCamera?.let {
            it.stopPreview()
            takePictureCallBack?.invoke(data) {bitmapData ->
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
            mCamera = Camera.open()
        }
        this.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                setCameraParams(surfaceTexture)

            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                if (mCamera != null) {
                    mCamera!!.stopPreview()
                    mCamera!!.release()
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
            val param = it.parameters
            param.pictureFormat = PixelFormat.JPEG
            param.flashMode = Camera.Parameters.FLASH_MODE_OFF
            if (Build.MODEL != "KORIDY H30") {
                param.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE// 1连续对焦
            } else {
                param.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }
            it.parameters = param
            //变形处理
            val previewRect = RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
            var aspect = mPreviewWidth.toDouble() / mPreviewHeight
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                aspect = 1 / aspect
            }
            if (mWidth < mHeight * aspect) {
                mDisplayWidth = mWidth
                mDisplayHeight = (mHeight * aspect + .5).toInt()
            } else {
                mDisplayWidth = (mWidth / aspect + .5).toInt()
                mDisplayHeight = mHeight
            }
            val surfaceDimensions =
                RectF(0f, 0f, mDisplayWidth.toFloat(), mDisplayHeight.toFloat())
            val matrix = Matrix()
            matrix.setRectToRect(previewRect, surfaceDimensions, Matrix.ScaleToFit.FILL)
            this@CameraTextureView.setTransform(matrix)
            //<-处理变形
            var displayRotation = 0
            val windowManager = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val rotation = windowManager.defaultDisplay.rotation
            when (rotation) {
                Surface.ROTATION_0 -> displayRotation = 0
                Surface.ROTATION_90 -> displayRotation = 90
                Surface.ROTATION_180 -> displayRotation = 180
                Surface.ROTATION_270 -> displayRotation = 270
            }
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(0, info)
            var orientation: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                orientation = (info.orientation - displayRotation + 360) % 360
            } else {
                orientation = (info.orientation + displayRotation) % 360
                orientation = (360 - orientation) % 360
            }
            it.parameters = param
            it.setDisplayOrientation(orientation)
            try {
                it.setPreviewTexture(surfaceTexture)
                it.startPreview()
                isCanTakePicture = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * 拍照
     */
    fun take() {
        if (mCamera != null && isCanTakePicture) {
            isCanTakePicture = false
            mCamera!!.takePicture(Camera.ShutterCallback { }, null, mPictureCallback)
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

    private inner class FileSaver(private val buffer: ByteArray) : Runnable {

        fun save() {
            Thread(this).start()
        }

        override fun run() {
            try {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    System.currentTimeMillis().toString() + ".png"
                )
                file.createNewFile()
                val os = FileOutputStream(file)
                val bos = BufferedOutputStream(os)
                val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                bos.flush()
                bos.close()
                os.close()
                this@CameraTextureView.setBackgroundDrawable(BitmapDrawable(bitmap))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}

