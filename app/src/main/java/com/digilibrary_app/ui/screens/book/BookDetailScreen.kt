package com.digilibrary_app.ui.screens.book

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.digilibrary_app.data.model.BookDto
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookIdOrSlug: String,
    onBack: () -> Unit,
    onReadBook: (book: BookDto) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var book by remember { mutableStateOf<BookDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bookIdOrSlug) {
        coroutineScope.launch {
            try {
                val api = ApiClient.getService(context)
                val res = api.getBookDetail(bookIdOrSlug)
                isLoading = false
                if (res.isSuccessful && res.body()?.data != null) {
                    book = res.body()!!.data
                } else {
                    errorMessage = res.body()?.message ?: "Gagal memuat detail buku."
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Koneksi gagal: ${e.localizedMessage}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.judul ?: "Detail Buku", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (book != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = SurfaceWhite
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { onReadBook(book!!) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka & Baca E-Book Sekarang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryEmerald)
            }
        } else if (errorMessage != null || book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = errorMessage ?: "Buku tidak ditemukan", color = SoftRed)
            }
        } else {
            val b = book!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Cover preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(PrimaryEmeraldDark, PrimaryEmerald)
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .height(260.dp)
                    ) {
                        if (!b.coverUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = b.coverUrl,
                                contentDescription = b.judul,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Category & Grade badges
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (b.tingkatKelas != null) {
                            Surface(
                                color = PrimaryEmerald,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Kelas ${b.tingkatKelas}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (b.category != null) {
                            Surface(
                                color = AccentMintSoft,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = b.category.nama,
                                    color = PrimaryEmeraldDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = b.judul,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Penulis: ${b.penulis}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Cards row: Rating, Pages, Read count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceWhite
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = RatingStar, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = b.rating ?: "5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text(text = "Rating", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceWhite
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${b.totalHalaman ?: 0}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Halaman", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceWhite
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${b.totalDibaca}x", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Dibaca", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Information details
                    Text(
                        text = "Sinopsis & Deskripsi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = b.deskripsi ?: "Buku teks Kurikulum Merdeka resmi dari SIBI Kemendikdasmen untuk menunjang aktivitas belajar dan membaca siswa.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!b.penerbit.isNullOrEmpty() || !b.isbn.isNullOrEmpty()) {
                        Text(
                            text = "Informasi Buku",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!b.penerbit.isNullOrEmpty()) {
                            Text(text = "• Penerbit: ${b.penerbit}", fontSize = 13.sp, color = TextSecondary)
                        }
                        if (b.tahunTerbit != null) {
                            Text(text = "• Tahun Terbit: ${b.tahunTerbit}", fontSize = 13.sp, color = TextSecondary)
                        }
                        if (!b.isbn.isNullOrEmpty()) {
                            Text(text = "• ISBN: ${b.isbn}", fontSize = 13.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
