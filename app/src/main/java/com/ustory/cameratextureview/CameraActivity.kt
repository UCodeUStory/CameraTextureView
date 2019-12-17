package com.ustory.cameratextureview

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        textureview.takePictureCallBack = { imageData, saveFinishCallBack ->
            PictureSaver.build {
                data = imageData
                callBack = saveFinishCallBack
            }.create().save()
        }

        iv_take_photo.setOnClickListener {
            tv_re_shooting.visibility = View.VISIBLE
            tv_cancel_shooting.visibility = View.GONE
            tv_confirm.visibility = View.VISIBLE
            textureview.take()
        }
        tv_cancel_shooting.setOnClickListener { finish() }
        tv_re_shooting.setOnClickListener {
            tv_cancel_shooting.visibility = View.VISIBLE
            tv_re_shooting.visibility = View.GONE
            tv_confirm.visibility = View.GONE
            textureview.reset()
        }
        tv_confirm.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        textureview.destroy()
        super.onDestroy()
    }
}
