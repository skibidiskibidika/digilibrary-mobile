package com.digilibrary_app.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.digilibrary_app.data.model.StudentReaderDto
import com.digilibrary_app.data.model.TeacherOverviewData
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherOverviewScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var overviewData by remember { mutableStateOf<TeacherOverviewData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var studentFilterTab by remember { mutableStateOf(0) } // 0: Semua, 1: Sudah Membaca, 2: Belum Membaca

    fun loadOverview() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val api = ApiClient.getService(context)
                val res = api.getTeacherOverview()
                isLoading = false
                if (res.isSuccessful && res.body()?.data != null) {
                    overviewData = res.body()!!.data
                } else {
                    errorMessage = res.body()?.message ?: "Gagal memuat ringkasan pantau kelas."
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadOverview()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantau Kelas Binaan", fontWeight = FontWeight.Bold) },
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
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryEmerald)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = errorMessage!!, color = SoftRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { loadOverview() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            } else if (overviewData != null) {
                val data = overviewData!!

                if (!data.hasAssignedClass) {
                    // Guru belum memiliki rombel binaan
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = SoftAmberLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = SoftAmber,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Belum Ditugaskan Sebagai Wali Kelas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Akun Anda terdaftar sebagai Guru namun belum memiliki penugasan rombel binaan dari Administrator Sekolah. Silakan hubungi admin sekolah untuk penugasan kelas.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    // Kelas aktif
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Class Banner
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(HeroGradientStart, HeroGradientEnd)
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = data.currentClass?.name ?: data.teacherAssignedClass ?: "Kelas Binaan",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Wali Kelas: ${data.currentClass?.code ?: ""}",
                                            fontSize = 13.sp,
                                            color = AccentMintLight
                                        )

                                        Spacer(modifier = Modifier.height(18.dp))

                                        // Stats Grid (4 items)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            StatBox(
                                                title = "Total Murid",
                                                value = "${data.stats?.totalStudents ?: 0}",
                                                modifier = Modifier.weight(1f)
                                            )
                                            StatBox(
                                                title = "Aktif Hari Ini",
                                                value = "${data.stats?.activeStudents ?: 0}",
                                                modifier = Modifier.weight(1f)
                                            )
                                            StatBox(
                                                title = "Progres Rata2",
                                                value = "${data.stats?.averageProgress ?: 0}%",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Leaderboard Section
                        if (data.leaderboard.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🏆 Papan Peringkat Literasi Kelas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            }

                            items(data.leaderboard) { student ->
                                StudentCard(student = student, isRanked = true)
                            }
                        }

                        // All Students List Header with Tab Filter
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Daftar Aktivitas Siswa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TabRow(
                                selectedTabIndex = studentFilterTab,
                                containerColor = SurfaceWhite,
                                contentColor = PrimaryEmerald
                            ) {
                                Tab(
                                    selected = studentFilterTab == 0,
                                    onClick = { studentFilterTab = 0 },
                                    text = { Text("Semua (${data.students.size})", fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = studentFilterTab == 1,
                                    onClick = { studentFilterTab = 1 },
                                    text = { Text("Sudah Baca", fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = studentFilterTab == 2,
                                    onClick = { studentFilterTab = 2 },
                                    text = { Text("Belum Baca", fontSize = 12.sp) }
                                )
                            }
                        }

                        val filteredStudents = when (studentFilterTab) {
                            1 -> data.students.filter { it.statusToday == "active" || it.todayDurationMinutes > 0 }
                            2 -> data.students.filter { it.statusToday != "active" && it.todayDurationMinutes == 0 }
                            else -> data.students
                        }

                        items(filteredStudents) { student ->
                            StudentCard(student = student, isRanked = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun StudentCard(
    student: StudentReaderDto,
    isRanked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge or avatar
            if (isRanked && student.rank != null) {
                Surface(
                    color = when (student.rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> PrimaryEmeraldLight
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${student.rank}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = PrimaryEmerald.copy(alpha = 0.15f)
                ) {
                    if (!student.avatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = student.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.name.firstOrNull()?.uppercase() ?: "S",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmeraldDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (!student.badge.isNullOrEmpty()) {
                    Text(
                        text = student.badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryEmeraldDark
                    )
                }
                Text(
                    text = "NIS: ${student.nis ?: "-"} • ${student.booksCount} buku dibaca",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Status Hari Ini: Sudah Membaca (Hijau) vs Belum Membaca (Merah)
            val isReadToday = student.statusToday == "active" || student.todayDurationMinutes > 0
            Surface(
                color = if (isReadToday) AccentMintSoft else SoftRedLight,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isReadToday) "Sudah Baca" else "Belum Baca",
                    color = if (isReadToday) PrimaryEmeraldDark else SoftRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
