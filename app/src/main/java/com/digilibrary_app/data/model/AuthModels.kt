package com.digilibrary_app.data.model

import com.google.gson.annotations.SerializedName

data class AuthData(
    @SerializedName("user") val user: UserDto,
    @SerializedName("token") val token: String,
    @SerializedName("is_profile_complete") val isProfileComplete: Boolean? = true
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("kelas") val kelas: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("nis") val nis: String?,
    @SerializedName("nisn") val nisn: String?,
    @SerializedName("nip") val nip: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class GoogleLoginRequest(
    @SerializedName("token") val token: String? = null,
    @SerializedName("id_token") val idToken: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

data class CompleteProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("kelas") val kelas: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("nis") val nis: String? = null,
    @SerializedName("nisn") val nisn: String? = null,
    @SerializedName("nip") val nip: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("remove_avatar") val removeAvatar: Boolean? = null
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String?,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String
)
