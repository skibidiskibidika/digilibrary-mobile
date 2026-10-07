package com.digilibrary_app.data.model

import com.google.gson.annotations.SerializedName

data class TeacherOverviewData(
    @SerializedName("has_assigned_class") val hasAssignedClass: Boolean,
    @SerializedName("current_class") val currentClass: ClassInfoDto?,
    @SerializedName("teacher_assigned_class") val teacherAssignedClass: String?,
    @SerializedName("stats") val stats: ClassStatsDto?,
    @SerializedName("leaderboard") val leaderboard: List<StudentReaderDto> = emptyList(),
    @SerializedName("students") val students: List<StudentReaderDto> = emptyList()
)

data class ClassInfoDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("sub") val sub: String?
)

data class ClassStatsDto(
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("active_students") val activeStudents: Int = 0,
    @SerializedName("average_progress") val averageProgress: Int = 0,
    @SerializedName("total_books_read") val totalBooksRead: Int = 0,
    @SerializedName("total_duration_minutes") val totalDurationMinutes: Int = 0
)

data class StudentReaderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("kelas") val kelas: String?,
    @SerializedName("nis") val nis: String?,
    @SerializedName("nisn") val nisn: String?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("badge") val badge: String?,
    @SerializedName("books_count") val booksCount: Int = 0,
    @SerializedName("total_duration_minutes") val totalDurationMinutes: Int = 0,
    @SerializedName("avg_progress") val avgProgress: Int = 0,
    @SerializedName("status_today") val statusToday: String = "inactive",
    @SerializedName("status_today_label") val statusTodayLabel: String = "Belum Membaca",
    @SerializedName("today_duration_minutes") val todayDurationMinutes: Int = 0,
    @SerializedName("last_book") val lastBook: String? = null
)
