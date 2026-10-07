package com.digilibrary_app.data.remote

import com.digilibrary_app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTHENTICATION ---
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/google")
    suspend fun loginGoogle(
        @Body request: GoogleLoginRequest
    ): Response<ApiResponse<AuthData>>

    @PUT("auth/complete-profile")
    suspend fun completeProfile(
        @Body request: CompleteProfileRequest
    ): Response<ApiResponse<UserDto>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<Map<String, Any>>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<Any>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<Any>>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Any>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    // --- USER PROFILE & HISTORY ---
    @POST("user/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<Map<String, Any>>>

    @Multipart
    @POST("user/profile")
    suspend fun updateProfileWithAvatar(
        @Part("name") name: RequestBody,
        @Part("nis") nis: RequestBody?,
        @Part("nisn") nisn: RequestBody?,
        @Part("nip") nip: RequestBody?,
        @Part avatarFile: MultipartBody.Part?
    ): Response<ApiResponse<Map<String, Any>>>

    @PUT("user/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Any>>

    @GET("user/reading-history")
    suspend fun getReadingHistory(): Response<ApiResponse<List<ReadingHistoryItemDto>>>

    // --- BOOKS & CATEGORIES ---
    @GET("categories")
    suspend fun getCategories(
        @Query("tipe") tipe: String? = null
    ): Response<ApiResponse<List<CategoryDto>>>

    @GET("books")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("kategori") kategoriId: Int? = null,
        @Query("tingkat_kelas") tingkatKelas: Int? = null,
        @Query("sort_by") sortBy: String? = "created_at",
        @Query("sort_order") sortOrder: String? = "desc",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 24
    ): Response<ApiResponse<PaginatedBooks>>

    @GET("books/{slug_or_id}")
    suspend fun getBookDetail(
        @Path("slug_or_id") slugOrId: String
    ): Response<ApiResponse<BookDto>>

    @POST("books/{id}/track-read")
    suspend fun trackRead(
        @Path("id") bookId: Int,
        @Body request: TrackReadRequest
    ): Response<ApiResponse<TrackReadResult>>

    @Streaming
    @GET("books/{id}/stream")
    suspend fun streamPdf(
        @Path("id") bookId: Int
    ): Response<ResponseBody>

    // --- GURU / WALI KELAS DASHBOARD ---
    @GET("guru/overview")
    suspend fun getTeacherOverview(): Response<ApiResponse<TeacherOverviewData>>
}
