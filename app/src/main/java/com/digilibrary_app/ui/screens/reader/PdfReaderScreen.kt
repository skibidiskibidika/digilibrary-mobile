package com.digilibrary_app.ui.screens.reader

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digilibrary_app.data.local.SessionManager
import com.digilibrary_app.data.model.BookDto
import com.digilibrary_app.data.model.TrackReadRequest
import com.digilibrary_app.data.remote.ApiClient
import com.digilibrary_app.ui.components.IdleDialog
import com.digilibrary_app.ui.components.WatermarkCanvas
import com.digilibrary_app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    book: BookDto,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val user = remember { sessionManager.getUser() }
    val studentName = user?.name ?: "Siswa Digilibrary"

    // Anti-screenshot FLAG_SECURE
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // State
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(book.totalHalaman ?: 1) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    var isLoadingPdf by remember { mutableStateOf(true) }
    var pdfErrorMessage by remember { mutableStateOf<String?>(null) }

    // Reading Tracker & Anti-Cheat State
    var activeDurationSeconds by remember { mutableStateOf(0) }
    var idleSeconds by remember { mutableStateOf(0) }
    var isPausedByIdle by remember { mutableStateOf(false) }
    var currentLogId by remember { mutableStateOf<Int?>(null) }

    // Sync function
    fun syncReadingProgress(finalSync: Boolean = false) {
        if (activeDurationSeconds <= 0) return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val api = ApiClient.getService(context)
                val req = TrackReadRequest(
                    logId = currentLogId,
                    durationSeconds = activeDurationSeconds,
                    pageNumber = currentPage,
                    platform = "mobile"
                )
                val res = api.trackRead(book.id, req)
                if (res.isSuccessful && res.body()?.data != null) {
                    currentLogId = res.body()!!.data!!.logId
                }
            } catch (_: Exception) {
                // Silently retain duration for next sync
            }
        }
    }

    // Intercept Back Press to sync
    BackHandler {
        syncReadingProgress(finalSync = true)
        onClose()
    }

    // 1-second active timer and 60-second idle detector loop
    LaunchedEffect(isPausedByIdle) {
        while (!isPausedByIdle) {
            delay(1000)
            activeDurationSeconds += 1
            idleSeconds += 1

            // 60-second idle detection
            if (idleSeconds >= 60) {
                isPausedByIdle = true
            }

            // 30-second periodic sync
            if (activeDurationSeconds > 0 && activeDurationSeconds % 30 == 0) {
                syncReadingProgress()
            }
        }
    }

    // Load PDF file
    LaunchedEffect(book.id) {
        withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "book_${book.id}.pdf")
                if (!cacheFile.exists() || cacheFile.length() < 1024) {
                    // Download from stream endpoint or pdfUrl
                    val targetUrl = book.pdfUrl ?: "${sessionManager.getBaseUrl()}books/${book.id}/stream"
                    val token = sessionManager.getToken()

                    val reqBuilder = Request.Builder().url(targetUrl)
                    if (!token.isNullOrEmpty()) {
                        reqBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    val okHttpClient = OkHttpClient()
                    val resp = okHttpClient.newCall(reqBuilder.build()).execute()
                    if (resp.isSuccessful && resp.body != null) {
                        val input = resp.body!!.byteStream()
                        val fos = FileOutputStream(cacheFile)
                        input.copyTo(fos)
                        fos.close()
                        input.close()
                    }
                }

                if (cacheFile.exists() && cacheFile.length() > 0) {
                    val pfd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    fileDescriptor = pfd
                    pdfRenderer = renderer
                    totalPages = renderer.pageCount.coerceAtLeast(1)

                    // Render first page
                    val page = renderer.openPage(0)
                    val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    withContext(Dispatchers.Main) {
                        currentBitmap = bmp
                        isLoadingPdf = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isLoadingPdf = false
                        pdfErrorMessage = "File buku digital belum siap diunduh."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingPdf = false
                    pdfErrorMessage = "Gagal memuat dokumen PDF: ${e.localizedMessage}"
                }
            }
        }
    }

    // Function to render a specific page
    fun renderPage(pageIndexZeroBased: Int) {
        val renderer = pdfRenderer ?: return
        if (pageIndexZeroBased < 0 || pageIndexZeroBased >= renderer.pageCount) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val page = renderer.openPage(pageIndexZeroBased)
                val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                withContext(Dispatchers.Main) {
                    currentBitmap = bmp
                    currentPage = pageIndexZeroBased + 1
                    idleSeconds = 0 // Reset idle counter on page flip
                }
            } catch (_: Exception) {}
        }
    }

    // Clean up PdfRenderer on dispose
    DisposableEffect(pdfRenderer) {
        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (_: Exception) {}
        }
    }

    // Idle Pause Dialog
    if (isPausedByIdle) {
        IdleDialog(
            onResume = {
                idleSeconds = 0
                isPausedByIdle = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book.judul,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Halaman $currentPage dari $totalPages • ${activeDurationSeconds / 60}m ${activeDurationSeconds % 60}s membaca",
                            fontSize = 11.sp,
                            color = AccentMintLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        syncReadingProgress(finalSync = true)
                        onClose()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup Buku", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeroGradientEnd,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPage > 1) {
                                renderPage(currentPage - 2)
                            }
                        },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Halaman Sebelumnya")
                    }

                    Text(
                        text = "Halaman $currentPage / $totalPages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < totalPages) {
                                renderPage(currentPage)
                            }
                        },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Halaman Berikutnya")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF262626))
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Reset idle counter on any screen tap
                            idleSeconds = 0
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPdf) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryEmerald)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Menyiapkan E-Book Kurikulum Merdeka...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else if (pdfErrorMessage != null || currentBitmap == null) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = AccentMintLight,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pdfErrorMessage ?: "Buku sedang dipersiapkan.",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Retry
                            isLoadingPdf = true
                            pdfErrorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Text("Muat Ulang Dokumen")
                    }
                }
            } else {
                // PDF Page Native Canvas
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "Halaman $currentPage",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Anti-Piracy Dynamic Watermark Overlay
                    WatermarkCanvas(
                        studentName = studentName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
