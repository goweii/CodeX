package per.goweii.codex.scanner

import android.graphics.*
import android.util.Size
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.min

object ImageConverter {

    fun bitmapRotation90(bitmap: Bitmap): Bitmap? {
        return try {
            val canvasBitmap = Bitmap.createBitmap(bitmap.height, bitmap.width, bitmap.config)
            val paint = Paint()
            val canvas = Canvas(canvasBitmap)
            canvas.rotate(90F)
            canvas.translate(0F, -bitmap.height.toFloat())
            canvas.drawBitmap(bitmap, 0F, 0F, paint)
            canvasBitmap
        } catch (ex: OutOfMemoryError) {
            null
        }
    }

    fun bitmapRotation180(bitmap: Bitmap): Bitmap? {
        return try {
            val canvasBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
            val paint = Paint()
            val canvas = Canvas(canvasBitmap)
            canvas.rotate(180F)
            canvas.translate(-bitmap.width.toFloat(), -bitmap.height.toFloat())
            canvas.drawBitmap(bitmap, 0F, 0F, paint)
            canvasBitmap
        } catch (ex: OutOfMemoryError) {
            null
        }
    }

    fun bitmapRotation270(bitmap: Bitmap): Bitmap? {
        return try {
            val canvasBitmap = Bitmap.createBitmap(bitmap.height, bitmap.width, bitmap.config)
            val paint = Paint()
            val canvas = Canvas(canvasBitmap)
            canvas.rotate(-90F)
            canvas.translate(-bitmap.width.toFloat(), 0F)
            canvas.drawBitmap(bitmap, 0F, 0F, paint)
            canvasBitmap
        } catch (ex: OutOfMemoryError) {
            null
        }
    }

    fun imageToYByteArray(image: ImageProxy, reuseBuffer: ByteArray?): ByteArray {
        val width = image.width
        val height = image.height
        val plane = image.planes[0]
        val yBuffer = plane.buffer
        yBuffer.rewind()
        val ySize = yBuffer.remaining()
        val buffer = if (reuseBuffer?.size == ySize) {
            reuseBuffer
        } else {
            ByteArray(ySize)
        }
        var position = 0
        for (row in 0 until height) {
            yBuffer.get(buffer, position, width)
            position += width
            yBuffer.position(min(ySize, yBuffer.position() - width + plane.rowStride))
        }
        return buffer
    }

    fun imageToBitmap(image: ImageProxy): Bitmap? {
        val byteArray = imageToJpegByteArray(image) ?: return null
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.RGB_565
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, option)
        } catch (ex: OutOfMemoryError) {
            null
        }
    }

    fun jpegToBitmap(byteArray: ByteArray): Bitmap? {
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.RGB_565
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, option)
        } catch (ex: OutOfMemoryError) {
            null
        }
    }

    fun imageToJpegByteArray(image: ImageProxy): ByteArray? {
        var data: ByteArray? = null
        if (image.format == ImageFormat.JPEG) {
            data = jpegImageToJpegByteArray(image)
        } else if (image.format == ImageFormat.YUV_420_888) {
            data = yuvImageToJpegByteArray(image)
        }
        return data
    }

    fun jpegImageToJpegByteArray(image: ImageProxy): ByteArray? {
        val planes = image.planes
        val buffer = planes[0].buffer
        var data = ByteArray(buffer.capacity())
        buffer.rewind()
        buffer[data]
        if (shouldCropImage(image)) {
            data = cropByteArray(data, image.cropRect) ?: return data
        }
        return data
    }

    fun yuvImageToJpegByteArray(image: ImageProxy): ByteArray? {
        val nv21 = yuv420888toNv21(image, null)
        val crop = if (shouldCropImage(image)) image.cropRect else null
        return nv21ToJpeg(nv21, image.width, image.height, crop)
    }

    private fun shouldCropImage(image: ImageProxy): Boolean {
        val sourceSize = Size(image.width, image.height)
        val targetSize = Size(image.cropRect.width(), image.cropRect.height())
        return targetSize != sourceSize
    }

    fun nv21ToJpeg(nv21: ByteArray, width: Int, height: Int, cropRect: Rect?): ByteArray? {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val success = yuv.compressToJpeg(cropRect ?: Rect(0, 0, width, height), 100, out)
        if (!success) {
            return null
        }
        return out.toByteArray()
    }

    fun yuv420888toNv21(image: ImageProxy, reuseBuffer: ByteArray?): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val ySize = yBuffer.remaining()

        val bufferSize = ySize + image.width * image.height / 2
        val buffer = if (reuseBuffer?.size == bufferSize) {
            reuseBuffer
        } else {
            ByteArray(bufferSize)
        }

        var position = 0

        for (row in 0 until image.height) {
            yBuffer[buffer, position, image.width]
            position += image.width
            yBuffer.position(
                min(
                    ySize.toDouble(),
                    (yBuffer.position() - image.width + yPlane.rowStride).toDouble()
                )
                    .toInt()
            )
        }

        val chromaHeight = image.height / 2
        val chromaWidth = image.width / 2
        val vRowStride = vPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vPixelStride = vPlane.pixelStride
        val uPixelStride = uPlane.pixelStride

        val vLineBuffer = ByteArray(vRowStride)
        val uLineBuffer = ByteArray(uRowStride)
        for (row in 0 until chromaHeight) {
            vBuffer[vLineBuffer, 0, min(vRowStride, vBuffer.remaining())]
            uBuffer[uLineBuffer, 0, min(uRowStride, uBuffer.remaining())]
            var vLineBufferPosition = 0
            var uLineBufferPosition = 0
            for (col in 0 until chromaWidth) {
                buffer[position++] = vLineBuffer[vLineBufferPosition]
                buffer[position++] = uLineBuffer[uLineBufferPosition]
                vLineBufferPosition += vPixelStride
                uLineBufferPosition += uPixelStride
            }
        }

        return buffer
    }

    fun cropByteArray(data: ByteArray, cropRect: Rect?): ByteArray? {
        if (cropRect == null) {
            return data
        }
        try {
            val decoder = BitmapRegionDecoder.newInstance(
                data, 0, data.size,
                false
            )
            val bitmap = decoder.decodeRegion(cropRect, BitmapFactory.Options())
            decoder.recycle()
            val out = ByteArrayOutputStream()
            val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            if (!success) {
                return data
            }
            bitmap.recycle()
            return out.toByteArray()
        } catch (e: IllegalArgumentException) {
        } catch (e: IOException) {
        }
        return data
    }
}