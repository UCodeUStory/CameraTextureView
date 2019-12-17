package com.ustory.cameratextureview


import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import com.ustory.cameratextureview.controller.CameraOneController


class CameraTextureView(context: Context, attrs: AttributeSet) :
    TextureView(context, attrs), View.OnLayoutChangeListener {
    private var mWidth = 0
    private var mHeight = 0
    var takePictureCallBack: ((ByteArray, callBack: (Bitmap?) -> Unit) -> Unit)? = null
    private var mCameraOneController: CameraOneController = CameraOneController(context)

    init {
        mCameraOneController.openCamera()
        mCameraOneController.setTakePhotoListener(pictureCallback())
        this.surfaceTextureListener = SurfaceTextureListener()
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

    fun take() {
        mCameraOneController.take()
    }

    fun cancel() {
        mCameraOneController.startPreview()
    }

    fun destroy() {
        mCameraOneController.releaseCamera()
    }

    private fun pictureCallback(): Camera.PictureCallback {
        return Camera.PictureCallback { data, camera ->
            mCameraOneController.stopPreview()
            takePictureCallBack?.invoke(data) { bitmapData ->
                this.post {
                    this@CameraTextureView.setBackgroundDrawable(BitmapDrawable(bitmapData))
                }
            }
        }
    }

    inner class SurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            Log.i("tag", "onSurfaceTextureAvailable")
            mCameraOneController.setPreviewTexture(surfaceTexture)
            this@CameraTextureView.setBackgroundDrawable(null)
            mCameraOneController.startPreview()
        }

        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            Log.i("tag", "onSurfaceTextureDestroyed")
            mCameraOneController.releaseCamera()
            return true
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }
    }

    companion object {
        const val TAG = "CameraTextureView"
    }

}

