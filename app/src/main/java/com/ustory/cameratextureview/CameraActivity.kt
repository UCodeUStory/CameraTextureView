package com.ustory.cameratextureview

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureview.takePictureCallBack = { imageData,saveFinishCallBack->
            PictureSaver.build {
                data = imageData
                callBack  = saveFinishCallBack
            }.create().save()
        }

        iv_take_photo.setOnClickListener { textureview.take() }
        iv_cancel.setOnClickListener { textureview.startPreview() }

        iv_confirm.setOnClickListener { textureview.startPreview() }
    }

    override fun onStart() {
        super.onStart()
        textureview.startPreview()
    }

    override fun onStop() {
        textureview.stopPreview()
        super.onStop()
    }

    override fun onDestroy() {
        textureview.releaseTextureView()
        super.onDestroy()
    }
}