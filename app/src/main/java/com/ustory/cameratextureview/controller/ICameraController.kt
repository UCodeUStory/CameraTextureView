package com.ustory.cameratextureview.controller

interface ICameraController {

    fun openCamera()

    fun releaseCamera()

    fun startPreview()

    fun stopPreview()

    fun focusOnPoint(x:Int, y:Int, width:Int, height:Int)

    fun take()


}