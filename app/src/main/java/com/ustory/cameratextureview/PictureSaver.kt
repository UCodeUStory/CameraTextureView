package com.ustory.cameratextureview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor

class PictureSaver(
    private var savePath: String,
    private var buffer: ByteArray,
    private var threadPoolExecutor: ThreadPoolExecutor? = null,
    private var saveFinishCallBack: ((Bitmap?) -> Unit)? = null
) : Runnable {

    fun save() {
        if (buffer == null) {
            return
        }
        if (threadPoolExecutor == null) {
            Thread(this).start()
            Log.i("PictureSaver", "开启新线程")
        } else {
            threadPoolExecutor?.execute(this)
        }
    }

    override fun run() {
        val file = File(savePath)
        Log.i(CameraTextureView.TAG, "保存文件路径：${file.absoluteFile}")
        file.createNewFile()
        val os = FileOutputStream(file)
        val bos = BufferedOutputStream(os)
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            bos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bos.close()
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    //建造者类
    class Builder {

        var savePath: String? = null

        var data: ByteArray? = null

        var threadPoolExecutor: ThreadPoolExecutor? = null

        var callBack: ((Bitmap?) -> Unit)? = null

        fun create(): PictureSaver {
            if (data == null) {
                throw java.lang.Exception("图片数据为Null")
            }
            val defaultSavePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + System.currentTimeMillis().toString() + ".png"

            return PictureSaver(
                savePath ?: defaultSavePath,
                data!!,
                threadPoolExecutor,
                callBack
            )
        }
    }


    companion object {
        fun build(block: Builder.() -> Unit) = Builder().apply(block)
    }
}
