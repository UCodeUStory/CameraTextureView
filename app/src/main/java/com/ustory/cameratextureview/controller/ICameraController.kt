package com.ustory.cameratextureview.controller

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.TextureView

interface ICameraController {

    fun openCamera()

    fun releaseCamera()

    fun setPreviewTexture(surfaceTexture: SurfaceTexture)

    fun startPreview()

    fun stopPreview()

    fun focusOnPoint(x:Int, y:Int, width:Int, height:Int)

    fun take()

    fun setTakePhotoListener(pictureCallBack: Camera.PictureCallback)


}