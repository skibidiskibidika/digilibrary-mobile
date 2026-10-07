package com.digilibrary_app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import com.digilibrary_app.data.local.SessionManager
import com.digilibrary_app.data.model.BookDto
import com.digilibrary_app.data.model.CategoryDto
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.components.BookCard
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onBookClick: (BookDto) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val user = remember { sessionManager.getUser() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var books by remember { mutableStateOf<List<BookDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val api = ApiClient.getService(context)
                // Load categories
                val catRes = api.getCategories()
                if (catRes.isSuccessful && catRes.body()?.data != null) {
                    categories = catRes.body()!!.data!!
                }

                // Load books with filters
                val bookRes = api.getBooks(
                    search = if (searchQuery.isNotBlank()) searchQuery.trim() else null,
                    kategoriId = selectedCategoryId,
                    tingkatKelas = selectedGrade,
                    sortBy = "created_at",
                    sortOrder = "desc",
                    perPage = 50
                )
                isLoading = false
                if (bookRes.isSuccessful && bookRes.body()?.data != null) {
                    books = bookRes.body()!!.data!!.data
                } else {
                    errorMessage = bookRes.body()?.message ?: "Gagal memuat katalog buku."
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Gagal terhubung ke server: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(selectedGrade, selectedCategoryId) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Hero Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(HeroGradientStart, HeroGradientEnd)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Halo, ${user?.name ?: "Sahabat Pintar"} 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (user?.role == "guru") {
                                "Guru / Wali Kelas • ${user.kelas ?: "Belum Ada Kelas"}"
                            } else {
                                "Siswa • ${user?.kelas ?: "Kelas SD"}"
                            },
                            fontSize = 12.sp,
                            color = AccentMintLight
                        )
                    }

                    // Avatar
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        if (!user?.avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.avatar,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (user?.name?.firstOrNull() ?: 'S').uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari judul buku, mata pelajaran, atau penulis...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryEmerald)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                loadData()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        } else {
                            IconButton(onClick = { loadData() }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Cari", tint = PrimaryEmerald)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = AccentMintLight,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // Grades & Categories horizontal scroll filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(vertical = 12.dp)
        ) {
            // Grade Filter Chips (Kelas 1 - 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGrade == null,
                    onClick = { selectedGrade = null },
                    label = { Text("Semua Jenjang") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryEmerald,
                        selectedLabelColor = Color.White
                    )
                )
                (1..6).forEach { grade ->
                    FilterChip(
                        selected = selectedGrade == grade,
                        onClick = {
                            selectedGrade = if (selectedGrade == grade) null else grade
                        },
                        label = { Text("Kelas $grade") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryEmerald,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Categories Filter Chips
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Semua Kategori") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryEmeraldDark,
                            selectedLabelColor = Color.White
                        )
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                            },
                            label = { Text(cat.nama) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryEmeraldDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Books Grid Section
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isLoading) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryEmerald)
                    }
                }
            } else if (errorMessage != null) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = SoftRed,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { loadData() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            } else if (books.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada buku yang ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Coba ubah kata kunci atau filter kelas Anda",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(books) { book ->
                    BookCard(
                        book = book,
                        onClick = { onBookClick(book) }
                    )
                }
            }
        }
    }
}
