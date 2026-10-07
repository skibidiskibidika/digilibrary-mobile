package com.digilibrary_app.data.model

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("tipe") val tipe: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("books_count") val booksCount: Int?
)

data class PaginatedBooks(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("data") val data: List<BookDto>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("total") val total: Int
)

data class BookDto(
    @SerializedName("id") val id: Int,
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("judul") val judul: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("penulis") val penulis: String,
    @SerializedName("penerbit") val penerbit: String?,
    @SerializedName("tahun_terbit") val tahunTerbit: Int?,
    @SerializedName("isbn") val isbn: String?,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("tingkat_kelas") val tingkatKelas: Int?,
    @SerializedName("total_halaman") val totalHalaman: Int?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("total_dibaca") val totalDibaca: Int = 0,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("pdf_url") val pdfUrl: String?,
    @SerializedName("category") val category: CategoryDto?
)

data class ReadingHistoryItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("book_id") val bookId: Int,
    @SerializedName("platform") val platform: String?,
    @SerializedName("halaman_terakhir") val halamanTerakhir: Int = 1,
    @SerializedName("durasi_detik") val durasiDetik: Int = 0,
    @SerializedName("read_at") val readAt: String?,
    @SerializedName("book") val book: BookDto?
)
