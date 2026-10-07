package com.digilibrary_app.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.digilibrary_app.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatermarkCanvas(
    studentName: String,
    modifier: Modifier = Modifier
) {
    val dateStr = rememberWatermarkDate()
    val watermarkText = "Digilibrary SD • $studentName • $dateStr"

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val nativeCanvas = drawContext.canvas.nativeCanvas
        val paint = Paint().apply {
            color = TextPrimary.copy(alpha = 0.13f).toArgb()
            textSize = 34f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val stepX = 420f
        val stepY = 280f

        val width = size.width
        val height = size.height

        var y = -height
        while (y < height * 2) {
            var x = -width
            while (x < width * 2) {
                nativeCanvas.save()
                nativeCanvas.rotate(-35f, x, y)
                nativeCanvas.drawText(watermarkText, x, y, paint)
                nativeCanvas.restore()
                x += stepX
            }
            y += stepY
        }
    }
}

@Composable
private fun rememberWatermarkDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return formatter.format(Date())
}
