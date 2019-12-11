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

    private fun initParams() {
        if (parameters == null) {
            parameters = mCamera?.parameters
        }
        setAutoFocus()
        parameters?.setRotation(90)
        setDisplayRotation()
        setPictureSize(preferredWidth, preferredHeight)
        setPreviewSize(preferredWidth, preferredHeight)
        commitParamters()
    }

    private fun setAutoFocus() {
        if (Build.MODEL != "KORIDY H30") {
            parameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE// 1连续对焦
        } else {
            parameters?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }
    }

    private fun setDisplayRotation() {
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
        mCamera?.setDisplayOrientation(orientation)
    }


    private fun setPreviewSize(width: Int, height: Int) {
        val optSize: Camera.Size
        if (parameters != null && mCamera != null && width > 0) {
            optSize =
                getOptimalSize(width, height, mCamera?.getParameters()!!.supportedPreviewSizes)
            Log.i("preview", "preview ${optSize.width}:${optSize.height}")
            parameters?.setPreviewSize(optSize.width, optSize.height)
        }
    }

    private fun commitParamters() {
        try {
            mCamera?.setParameters(parameters)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    private fun setPictureSize(width: Int, height: Int) {
        val pictureSize =
            getPictureSize(width, height, mCamera?.getParameters()!!.supportedPreviewSizes)
        parameters?.setPictureSize(pictureSize.width, pictureSize.height)
        Log.i("preview", "pictureSize ${pictureSize.width}:${pictureSize.height}")
    }

    private fun getOptimalSize(width: Int, height: Int, sizes: List<Camera.Size>): Camera.Size {

        val pictureSize = sizes[0]

        val candidates = ArrayList<Camera.Size>()

        for (size in sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size)
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size)
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min<Camera.Size>(candidates, sizeComparator)
        }

        for (size in sizes) {
            if (size.width > width && size.height > height) {
                return size
            }
        }

        return pictureSize
    }

    private fun getPictureSize(width: Int, height: Int, sizes: List<Camera.Size>): Camera.Size {

        val pictureSize = sizes[0]

        val candidates = ArrayList<Camera.Size>()

        for (size in sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size)
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size)
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.max<Camera.Size>(candidates, sizeComparator)
        }

        for (size in sizes) {
            if (size.width > width && size.height > height) {
                return size
            }
        }

        return pictureSize
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

    /**
     * 拍照
     */
    fun take() {
        if (mCamera != null && isCanTakePicture) {
            isCanTakePicture = false
            mCamera!!.takePicture(Camera.ShutterCallback { }, null, mPictureCallback)
        }
    }

    fun onResume() {
        initParams()
        startPreview()
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

    var isMunualRelease = false
    fun munualRelease() {
        isMunualRelease = true
        releaseTextureView()
    }

    fun munualInitTextureView() {
        if (isMunualRelease) {
            init()
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

