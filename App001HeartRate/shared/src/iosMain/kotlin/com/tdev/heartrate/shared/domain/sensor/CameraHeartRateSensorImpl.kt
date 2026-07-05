package com.tdev.heartrate.shared.domain.sensor

import kotlinx.cinterop.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.CoreVideo.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.darwin.dispatch_get_global_queue
import platform.posix.QOS_CLASS_USER_INITIATED
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
class CameraHeartRateSensorImpl : CameraHeartRateSensor {

    private var captureSession: AVCaptureSession? = null
    private var videoOutput: AVCaptureVideoDataOutput? = null
    private var delegate: FrameDelegate? = null

    override fun startMeasurement(): Flow<CameraMeasurementState> = callbackFlow {
        trySend(CameraMeasurementState(state = SensorState.INITIALIZING))
        var wasMeasuring = false

        try {
            captureSession = AVCaptureSession()
            val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
                deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
                mediaType = AVMediaTypeVideo,
                position = AVCaptureDevicePositionBack
            )
            val device = discoverySession.devices.firstOrNull() as? AVCaptureDevice

            if (device == null) {
                trySend(CameraMeasurementState(state = SensorState.ERROR, errorMessage = "Camera not available"))
                close()
                return@callbackFlow
            }

            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureInput
            if (captureSession?.canAddInput(input) == true) {
                captureSession?.addInput(input)
            }

            videoOutput = AVCaptureVideoDataOutput()
            val videoSettings = mapOf<Any?, Any?>(
                platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey to platform.CoreVideo.kCVPixelFormatType_32BGRA
            )
            videoOutput?.videoSettings = videoSettings

            // Xóa frame cũ ngay lập tức nếu queue xử lý không kịp để tránh lag
            videoOutput?.alwaysDiscardsLateVideoFrames = true

            val redValues = mutableListOf<Float>()
            val timestamps = mutableListOf<Long>()

            delegate = FrameDelegate { avgRed, isFingerDetected, currentTime, framesCount ->
                if (!isFingerDetected) {
                    if (wasMeasuring) {
                        trySend(CameraMeasurementState(state = SensorState.FAILED))
                        wasMeasuring = false
                        redValues.clear()
                        timestamps.clear()
                    } else {
                        trySend(CameraMeasurementState(state = SensorState.NO_FINGER))
                    }
                } else {
                    wasMeasuring = true
                    redValues.add(avgRed)
                    timestamps.add(currentTime)

                    if (redValues.size > 150) {
                        redValues.removeAt(0)
                        timestamps.removeAt(0)
                    }

                    val progress = (framesCount / 300f).coerceAtMost(1.0f)

                    if (redValues.size > 60) {
                        val bpm = calculateBpm(redValues, timestamps)

                        if (progress >= 1.0f) {
                            if (bpm in 40..200) {
                                trySend(CameraMeasurementState(bpm = bpm, state = SensorState.COMPLETED, progress = 1.0f))
                            } else {
                                trySend(CameraMeasurementState(state = SensorState.FAILED))
                                wasMeasuring = false
                            }
                        } else {
                            val validBpm = if (bpm in 40..200) bpm else 0
                            trySend(CameraMeasurementState(bpm = validBpm, state = SensorState.MEASURING, progress = progress))
                        }
                    } else {
                        trySend(CameraMeasurementState(state = SensorState.MEASURING, progress = progress))
                    }
                }
            }

            // Đẩy việc xử lý ảnh xuống Background Queue
            val queue = dispatch_get_global_queue(QOS_CLASS_USER_INITIATED.toLong(), 0uL)
            videoOutput?.setSampleBufferDelegate(delegate as AVCaptureVideoDataOutputSampleBufferDelegateProtocol, queue)

            if (captureSession?.canAddOutput(videoOutput!!) == true) {
                captureSession?.addOutput(videoOutput!!)
            }

            captureSession?.startRunning()

            if (device.hasTorch && device.isTorchModeSupported(AVCaptureTorchModeOn)) {
                device.lockForConfiguration(null)
                device.torchMode = AVCaptureTorchModeOn
                device.unlockForConfiguration()
            }

        } catch (e: Exception) {
            trySend(CameraMeasurementState(state = SensorState.ERROR, errorMessage = e.message ?: "Unknown Error"))
        }

        awaitClose {
            stopMeasurement()
        }
    }

    private fun calculateBpm(values: List<Float>, times: List<Long>): Int {
        if (values.size < 10) return 0

        // Lọc Trung Bình Động (Moving Average)
        val windowSize = 5
        val smoothedValues = mutableListOf<Float>()
        for (i in 0..values.size - windowSize) {
            var sum = 0f
            for (j in 0 until windowSize) {
                sum += values[i + j]
            }
            smoothedValues.add(sum / windowSize)
        }

        // Tìm đỉnh trên đồ thị đã làm mượt
        var peaks = 0
        for (i in 1 until smoothedValues.size - 1) {
            if (smoothedValues[i] > smoothedValues[i - 1] && smoothedValues[i] > smoothedValues[i + 1]) {
                peaks++
            }
        }

        val durationSec = (times.last() - times.first()) / 1000f
        if (durationSec <= 0 || peaks < 2) return 0

        val estimatedBpm = (peaks / durationSec) * 60f
        return estimatedBpm.roundToInt().coerceIn(40, 200)
    }

    override fun stopMeasurement() {
        captureSession?.stopRunning()

        val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionBack
        )
        val device = discoverySession.devices.firstOrNull() as? AVCaptureDevice
        if (device?.hasTorch == true && device.isTorchModeSupported(AVCaptureTorchModeOff)) {
            device.lockForConfiguration(null)
            device.torchMode = AVCaptureTorchModeOff
            device.unlockForConfiguration()
        }

        captureSession = null
        videoOutput = null
        delegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class FrameDelegate(
    private val onProcess: (Float, Boolean, Long, Int) -> Unit
) : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    private var framesCount = 0

    override fun captureOutput(
        output: platform.AVFoundation.AVCaptureOutput,
        didOutputSampleBuffer: platform.CoreMedia.CMSampleBufferRef?,
        fromConnection: platform.AVFoundation.AVCaptureConnection
    ) {
        val imageBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return

        // SỬA LỖI 1: Dùng toULong() cho cờ Lock
        CVPixelBufferLockBaseAddress(imageBuffer, kCVPixelBufferLock_ReadOnly.toULong())

        val baseAddress = CVPixelBufferGetBaseAddress(imageBuffer)

        // SỬA LỖI 2: Ép kiểu toàn bộ kích thước về Int ngay khi lấy ra để dễ tính toán
        val bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer).toInt()
        val width = CVPixelBufferGetWidth(imageBuffer).toInt()
        val height = CVPixelBufferGetHeight(imageBuffer).toInt()

        if (baseAddress != null) {
            val buffer = baseAddress.reinterpret<ByteVar>()
            var sumRed = 0L
            var sumGreen = 0L
            var sumBlue = 0L
            var count = 0

            // Vì width và height giờ đã là Int, các phép toán này sẽ không còn báo đỏ nữa
            val startX = width / 4
            val endX = (width * 3) / 4
            val startY = height / 4
            val endY = (height * 3) / 4

            for (y in startY until endY step 4) {
                for (x in startX until endX step 4) {
                    val pixelOffset = y * bytesPerRow + x * 4

                    sumBlue += (buffer[pixelOffset].toInt() and 0xFF)
                    sumGreen += (buffer[pixelOffset + 1].toInt() and 0xFF)
                    sumRed += (buffer[pixelOffset + 2].toInt() and 0xFF)
                    count++
                }
            }

            val avgRed = if (count > 0) sumRed.toFloat() / count else 0f
            val avgGreen = if (count > 0) sumGreen.toFloat() / count else 0f
            val avgBlue = if (count > 0) sumBlue.toFloat() / count else 0f

            println("HeartRate_Debug -> R: $avgRed, G: $avgGreen, B: $avgBlue")

            val isFingerDetected = avgRed > 100f

            val currentTime = (NSDate().timeIntervalSince1970() * 1000).toLong()
            framesCount++

            onProcess(avgRed, isFingerDetected, currentTime, framesCount)
        }

        // SỬA LỖI 3: Dùng toULong() cho cờ Unlock
        CVPixelBufferUnlockBaseAddress(imageBuffer, kCVPixelBufferLock_ReadOnly.toULong())
    }
}