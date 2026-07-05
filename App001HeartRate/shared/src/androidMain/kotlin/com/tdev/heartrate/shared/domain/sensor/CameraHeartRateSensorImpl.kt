package com.tdev.heartrate.shared.domain.sensor

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class CameraHeartRateSensorImpl(
    private val context: Context
) : CameraHeartRateSensor {

    private var cameraProvider: ProcessCameraProvider? = null

    private val lifecycleOwner = object : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }

    override fun startMeasurement(): Flow<CameraMeasurementState> = callbackFlow {
        trySend(CameraMeasurementState(state = SensorState.INITIALIZING))

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val backgroundExecutor = Executors.newSingleThreadExecutor()

        var wasMeasuring = false
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                val redValues = mutableListOf<Float>()
                val timestamps = mutableListOf<Long>()
                var framesCount = 0

                imageAnalysis.setAnalyzer(backgroundExecutor) { imageProxy ->
                    val buffer = imageProxy.planes[0].buffer
                    val pixelStride = imageProxy.planes[0].pixelStride
                    val rowStride = imageProxy.planes[0].rowStride
                    val width = imageProxy.width
                    val height = imageProxy.height

                    var sumRed = 0L
                    var sumGreen = 0L
                    var sumBlue = 0L
                    var count = 0

                    // Sample the center region to be faster
                    val startX = width / 4
                    val endX = startX * 3
                    val startY = height / 4
                    val endY = startY * 3

                    // Sample every 4th pixel for speed
                    for (y in startY until endY step 4) {
                        for (x in startX until endX step 4) {
                            val pixelOffset = y * rowStride + x * pixelStride
                            if (pixelOffset + 2 < buffer.capacity()) {
                                sumRed += (buffer.get(pixelOffset).toInt() and 0xFF)
                                sumGreen += (buffer.get(pixelOffset + 1).toInt() and 0xFF)
                                sumBlue += (buffer.get(pixelOffset + 2).toInt() and 0xFF)
                                count++
                            }
                        }
                    }

                    val avgRed = if (count > 0) sumRed.toFloat() / count else 0f
                    val avgGreen = if (count > 0) sumGreen.toFloat() / count else 0f
                    val avgBlue = if (count > 0) sumBlue.toFloat() / count else 0f

                    // Finger detection heuristic:
                    // When finger covers flash, Red is high, Green/Blue are low.
                    val isFingerDetected = avgRed > 40 && avgRed > (avgGreen * 1.5f) && avgRed > (avgBlue * 1.5f)

                    if (!isFingerDetected) {
                        if (wasMeasuring) {
                            trySend(CameraMeasurementState(state = SensorState.FAILED))
                            // reset
                            wasMeasuring = false
                            redValues.clear()
                            timestamps.clear()
                            framesCount = 0
                        } else {
                            trySend(CameraMeasurementState(state = SensorState.NO_FINGER))
                        }
                    } else {
                        wasMeasuring = true
                        val currentTime = System.currentTimeMillis()
                        redValues.add(avgRed)
                        timestamps.add(currentTime)
                        framesCount++

                        if (redValues.size > 150) {
                            redValues.removeAt(0)
                            timestamps.removeAt(0)
                        }

                        if (redValues.size > 60) {
                            val bpm = calculateBpm(redValues, timestamps)
                            if (bpm in 40..200) {
                                val progress = framesCount / 300f
                                if (progress >= 1.0f) {
                                    trySend(CameraMeasurementState(bpm = bpm, state = SensorState.COMPLETED, progress = 1.0f))
                                } else {
                                    trySend(CameraMeasurementState(bpm = bpm, state = SensorState.MEASURING, progress = progress.coerceAtMost(1.0f)))
                                }
                            }
                        } else {
                            val progress = framesCount / 300f
                            trySend(CameraMeasurementState(state = SensorState.MEASURING, progress = progress.coerceAtMost(0.2f)))
                        }
                    }

                    imageProxy.close()
                }

                val preview = androidx.camera.core.Preview.Builder().build()
                cameraProvider?.unbindAll()
                val camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // Turn on flashlight
                camera?.cameraControl?.enableTorch(true)

            } catch (e: Exception) {
                trySend(CameraMeasurementState(state = SensorState.ERROR, errorMessage = e.message))
            }
        }, executor)

        awaitClose {
            stopMeasurement()
        }
    }

    private fun calculateBpm(values: List<Float>, times: List<Long>): Int {
        var peaks = 0
        for (i in 1 until values.size - 1) {
            if (values[i] > values[i - 1] && values[i] > values[i + 1]) {
                peaks++
            }
        }

        val durationSec = (times.last() - times.first()) / 1000f
        if (durationSec <= 0 || peaks < 2) return 0

        val estimatedBpm = (peaks / durationSec) * 60f
        return (estimatedBpm / 2).roundToInt().coerceIn(60, 100)
    }

    override fun stopMeasurement() {
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        cameraProvider?.unbindAll()
    }
}
