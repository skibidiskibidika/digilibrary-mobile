package com.digilibrary_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digilibrary_app.ui.theme.PrimaryEmerald
import com.digilibrary_app.ui.theme.TextPrimary
import com.digilibrary_app.ui.theme.TextSecondary

@Composable
fun IdleDialog(
    onResume: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force user to tap Resume button to continue */ },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        icon = {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = PrimaryEmerald,
                modifier = Modifier.size(44.dp)
            )
        },
        title = {
            Text(
                text = "Sesi Membaca Dijeda",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Sesi membaca dijeda karena layar tidak disentuh selama lebih dari 60 detik. Sentuh tombol di bawah untuk melanjutkan aktivitas membaca Anda.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text(
                        text = "Lanjut Membaca",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    )
}
