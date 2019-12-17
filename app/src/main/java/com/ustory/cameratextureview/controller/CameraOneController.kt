package com.ustory.cameratextureview.controller

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import com.ustory.cameratextureview.CameraUtils
import java.util.*

class CameraOneController(val context: Context) : ICameraController {

    var mCamera: Camera? = null

    var parameters: Camera.Parameters? = null

    private var preferredWidth = 1280
    private var preferredHeight = 720

    private var isCanTakePicture = false

    private var mPictureCallBack: Camera.PictureCallback? = null

    override fun openCamera() {
        mCamera = Camera.open()
        initParams()
    }

    override fun releaseCamera() {
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
        isCanTakePicture = true
    }

    override fun startPreview() {
        if (mCamera != null && !isCanTakePicture) {
            mCamera?.startPreview()
            isCanTakePicture = true
        }
    }

    override fun stopPreview() {
        mCamera?.stopPreview()
    }

    override fun setTakePhotoListener(pictureCallBack: Camera.PictureCallback) {
        this.mPictureCallBack = pictureCallBack
    }

    override fun setPreviewTexture(surfaceTexture: SurfaceTexture) {
        mCamera?.setPreviewTexture(surfaceTexture)
    }

    override fun focusOnPoint(x: Int, y: Int, width: Int, height: Int) {
    }

    override fun take() {
        if (mCamera != null && isCanTakePicture) {
            isCanTakePicture = false
            if (mPictureCallBack == null) {
                mPictureCallBack = object : Camera.PictureCallback {
                    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                    }
                }
            }
            mCamera?.takePicture(Camera.ShutterCallback { }, null, mPictureCallBack)
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

        sizes.forEach {
            Log.i("preview", "optimalSize=${it.width}x${it.height}")
        }
        var pictureSize = sizes[0]

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
        /**
         * 查找等比例宽高
         */
        if (!candidates.isEmpty()) {
            return Collections.max<Camera.Size>(candidates, CameraUtils.sizeComparator)
        }
        /**
         * 查找其他比例宽高
         */
        for (size in sizes) {
            if (size.width > pictureSize.width && size.height > pictureSize.height) {
                pictureSize = size
            }
        }

        return pictureSize
    }

    private fun getPictureSize(width: Int, height: Int, sizes: List<Camera.Size>): Camera.Size {

        var pictureSize = sizes[0]

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
        /**
         * 查找等比例宽高
         */
        if (!candidates.isEmpty()) {
            return Collections.max<Camera.Size>(candidates, CameraUtils.sizeComparator)
        }
        /**
         * 查找其他比例宽高
         */
        for (size in sizes) {
            if (size.width > pictureSize.width && size.height > pictureSize.height) {
                pictureSize = size
            }
        }

        return pictureSize
    }
}