package com.digilibrary_app.data.model

import com.google.gson.annotations.SerializedName

data class TrackReadRequest(
    @SerializedName("log_id") val logId: Int? = null,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("page_number") val pageNumber: Int,
    @SerializedName("platform") val platform: String = "mobile"
)

data class TrackReadResult(
    @SerializedName("total_dibaca") val totalDibaca: Int,
    @SerializedName("log_id") val logId: Int
)
