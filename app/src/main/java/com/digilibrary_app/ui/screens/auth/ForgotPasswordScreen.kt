package com.digilibrary_app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digilibrary_app.data.model.ForgotPasswordRequest
import com.digilibrary_app.data.model.ResetPasswordRequest
import com.digilibrary_app.data.model.VerifyOtpRequest
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.launch

enum class ResetStep {
    EMAIL_INPUT,
    OTP_INPUT,
    NEW_PASSWORD_INPUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(ResetStep.EMAIL_INPUT) }
    var email by remember { mutableStateOf("") }
    var otpToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atur Ulang Kata Sandi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Error banner
            if (errorMessage != null) {
                Surface(
                    color = SoftRedLight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SoftRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage!!, color = SoftRed, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success banner
            if (successMessage != null) {
                Surface(
                    color = AccentMintSoft,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = successMessage!!, color = PrimaryEmeraldDark, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (currentStep) {
                ResetStep.EMAIL_INPUT -> {
                    Text(
                        text = "Lupa Kata Sandi Akun?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Masukkan alamat email yang terdaftar. Kami akan mengirimkan 6-digit kode OTP ke email Anda.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Akun Anda") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryEmerald) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                errorMessage = "Mohon isi alamat email Anda."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val api = ApiClient.getService(context)
                                    val res = api.forgotPassword(ForgotPasswordRequest(email.trim()))
                                    isLoading = false
                                    if (res.isSuccessful) {
                                        successMessage = res.body()?.message ?: "Kode OTP telah dikirim ke email."
                                        currentStep = ResetStep.OTP_INPUT
                                    } else {
                                        errorMessage = res.body()?.message ?: "Gagal mengirimkan kode OTP."
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            Text("Kirim Kode OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                ResetStep.OTP_INPUT -> {
                    Text(
                        text = "Verifikasi Kode OTP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Masukkan 6-digit kode verifikasi yang dikirimkan ke $email",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = otpToken,
                        onValueChange = {
                            if (it.length <= 6) otpToken = it
                            errorMessage = null
                        },
                        label = { Text("Kode OTP (6 Digit)") },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = PrimaryEmerald) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (otpToken.length < 4) {
                                errorMessage = "Masukkan kode OTP yang valid."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val api = ApiClient.getService(context)
                                    val res = api.verifyOtp(VerifyOtpRequest(email.trim(), otpToken.trim()))
                                    isLoading = false
                                    if (res.isSuccessful) {
                                        successMessage = "Kode OTP valid! Silakan tentukan kata sandi baru."
                                        currentStep = ResetStep.NEW_PASSWORD_INPUT
                                    } else {
                                        errorMessage = res.body()?.message ?: "Kode OTP tidak valid atau kedaluwarsa."
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            Text("Verifikasi OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                ResetStep.NEW_PASSWORD_INPUT -> {
                    Text(
                        text = "Buat Kata Sandi Baru",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Atur kata sandi baru untuk akun $email.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("Kata Sandi Baru") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryEmerald) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Ulangi Kata Sandi Baru") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryEmerald) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (newPassword.isBlank()) {
                                errorMessage = "Kata sandi baru tidak boleh kosong."
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                errorMessage = "Konfirmasi kata sandi tidak cocok."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val api = ApiClient.getService(context)
                                    val res = api.resetPassword(
                                        ResetPasswordRequest(
                                            email = email.trim(),
                                            token = otpToken.trim(),
                                            password = newPassword,
                                            passwordConfirmation = confirmPassword
                                        )
                                    )
                                    isLoading = false
                                    if (res.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Kata sandi berhasil diatur ulang! Silakan masuk.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onNavigateBackToLogin()
                                    } else {
                                        errorMessage = res.body()?.message ?: "Gagal mengatur ulang kata sandi."
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            Text("Simpan Kata Sandi Baru", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
