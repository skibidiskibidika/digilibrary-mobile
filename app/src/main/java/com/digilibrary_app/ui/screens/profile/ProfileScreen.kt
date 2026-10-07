package com.digilibrary_app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.digilibrary_app.data.local.SessionManager
import com.digilibrary_app.data.model.ChangePasswordRequest
import com.digilibrary_app.data.model.UpdateProfileRequest
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.components.ServerConfigDialog
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    var user by remember { mutableStateOf(sessionManager.getUser()) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Edit Profile form state
    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editNis by remember { mutableStateOf(user?.nis ?: "") }
    var editNisn by remember { mutableStateOf(user?.nisn ?: "") }
    var editNip by remember { mutableStateOf(user?.nip ?: "") }

    // Change Password form state
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = sessionManager.getBaseUrl(),
            onDismiss = { showServerConfigDialog = false },
            onSave = { newUrl ->
                sessionManager.setBaseUrl(newUrl)
                ApiClient.resetClient()
                showServerConfigDialog = false
                Toast.makeText(context, "URL API diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profil Pengguna", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (user?.role == "guru") {
                        OutlinedTextField(
                            value = editNip,
                            onValueChange = { editNip = it },
                            label = { Text("NIP (Nomor Induk Pegawai)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = editNis,
                            onValueChange = { editNis = it },
                            label = { Text("NIS (Nomor Induk Siswa)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editNisn,
                            onValueChange = { editNisn = it },
                            label = { Text("NISN (Nasional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val api = ApiClient.getService(context)
                                val res = api.updateProfile(
                                    UpdateProfileRequest(
                                        name = editName.trim(),
                                        nis = if (user?.role == "siswa") editNis.trim() else null,
                                        nisn = if (user?.role == "siswa") editNisn.trim() else null,
                                        nip = if (user?.role == "guru") editNip.trim() else null
                                    )
                                )
                                if (res.isSuccessful) {
                                    val updatedUser = user?.copy(
                                        name = editName.trim(),
                                        nis = editNis.trim(),
                                        nisn = editNisn.trim(),
                                        nip = editNip.trim()
                                    )
                                    if (updatedUser != null) {
                                        sessionManager.updateUser(updatedUser)
                                        user = updatedUser
                                    }
                                    showEditProfileDialog = false
                                    Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, res.body()?.message ?: "Gagal memperbarui profil", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Koneksi gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Ubah Kata Sandi", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Kata Sandi Saat Ini") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Kata Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Ulangi Kata Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            try {
                                val api = ApiClient.getService(context)
                                val res = api.changePassword(
                                    ChangePasswordRequest(
                                        currentPassword = oldPassword.ifEmpty { null },
                                        newPassword = newPassword,
                                        newPasswordConfirmation = confirmPassword
                                    )
                                )
                                if (res.isSuccessful) {
                                    showChangePasswordDialog = false
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    Toast.makeText(context, "Kata sandi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, res.body()?.message ?: "Gagal mengubah kata sandi", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Koneksi gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Ubah Sandi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun Digilibrary SD?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val api = ApiClient.getService(context)
                                api.logout()
                            } catch (_: Exception) {}
                            sessionManager.clear()
                            showLogoutDialog = false
                            onLogoutSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftRed)
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Pengguna", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = PrimaryEmeraldLight
                    ) {
                        if (!user?.avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.avatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.name?.firstOrNull()?.uppercase() ?: "U",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user?.name ?: "Pengguna Digilibrary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = user?.email ?: "",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = if (user?.role == "guru") SoftAmberLight else AccentMintSoft,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (user?.role == "guru") "Guru / Wali Kelas" else "Siswa Sekolah Dasar",
                            color = if (user?.role == "guru") SoftAmber else PrimaryEmeraldDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Academic Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Data Akademik Sekolah",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (user?.role == "guru") {
                        InfoRow(label = "NIP Pegawai", value = user?.nip ?: "Belum diisi")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderDivider)
                        InfoRow(label = "Kelas Binaan", value = user?.kelas ?: "Belum ditugaskan")
                    } else {
                        InfoRow(label = "Kelas Saat Ini", value = user?.kelas ?: "Belum ditentukan")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderDivider)
                        InfoRow(label = "NIS Siswa", value = user?.nis ?: "Belum diisi")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderDivider)
                        InfoRow(label = "NISN Nasional", value = user?.nisn ?: "Belum diisi")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextButton(
                        onClick = {
                            editName = user?.name ?: ""
                            editNis = user?.nis ?: ""
                            editNisn = user?.nisn ?: ""
                            editNip = user?.nip ?: ""
                            showEditProfileDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Data Diri", color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }

                    HorizontalDivider(color = BorderDivider)

                    TextButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ubah Kata Sandi", color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }

                    HorizontalDivider(color = BorderDivider)

                    TextButton(
                        onClick = { showServerConfigDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = PrimaryEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Konfigurasi Server API", color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = SoftRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
