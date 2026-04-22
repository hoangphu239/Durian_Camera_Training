package com.netsservices.dct.presentation.helper.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class GyroStabilityDetector(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var lastStableTime: Long? = null

    var isStable = false
        private set

    private val STABLE_THRESHOLD = 0.1f
    private val STABLE_DURATION = 100L

    fun start() {
        sensorManager.registerListener(
            this,
            gyro,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val rotation = kotlin.math.sqrt(x * x + y * y + z * z)

        val now = System.currentTimeMillis()

        if (rotation < STABLE_THRESHOLD) {
            if (lastStableTime == null) {
                lastStableTime = now
            }

            val stableTime = now - (lastStableTime ?: now)
            isStable = stableTime >= STABLE_DURATION

        } else {
            lastStableTime = null
            isStable = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}