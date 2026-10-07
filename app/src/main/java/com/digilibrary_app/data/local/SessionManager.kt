package com.digilibrary_app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.digilibrary_app.data.model.UserDto

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "digilibrary_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_KELAS = "user_kelas"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_USER_NIS = "user_nis"
        private const val KEY_USER_NISN = "user_nisn"
        private const val KEY_USER_NIP = "user_nip"
        private const val KEY_PROFILE_COMPLETE = "is_profile_complete"
        private const val KEY_BASE_URL = "custom_base_url"

        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/api/"
    }

    fun saveAuthData(token: String, user: UserDto, isProfileComplete: Boolean = true) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_KELAS, user.kelas)
            putString(KEY_USER_AVATAR, user.avatar)
            putString(KEY_USER_NIS, user.nis)
            putString(KEY_USER_NISN, user.nisn)
            putString(KEY_USER_NIP, user.nip)
            putBoolean(KEY_PROFILE_COMPLETE, isProfileComplete)
            apply()
        }
    }

    fun updateUser(user: UserDto) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_KELAS, user.kelas)
            putString(KEY_USER_AVATAR, user.avatar)
            putString(KEY_USER_NIS, user.nis)
            putString(KEY_USER_NISN, user.nisn)
            putString(KEY_USER_NIP, user.nip)
            apply()
        }
    }

    fun setProfileCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, completed).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, true)

    fun getUser(): UserDto? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        if (id == -1) return null
        return UserDto(
            id = id,
            name = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            role = prefs.getString(KEY_USER_ROLE, "siswa") ?: "siswa",
            kelas = prefs.getString(KEY_USER_KELAS, null),
            avatar = prefs.getString(KEY_USER_AVATAR, null),
            nis = prefs.getString(KEY_USER_NIS, null),
            nisn = prefs.getString(KEY_USER_NISN, null),
            nip = prefs.getString(KEY_USER_NIP, null)
        )
    }

    fun getBaseUrl(): String {
        val stored = prefs.getString(KEY_BASE_URL, null)
        return if (!stored.isNullOrBlank()) {
            if (stored.endsWith("/")) stored else "$stored/"
        } else {
            DEFAULT_BASE_URL
        }
    }

    fun setBaseUrl(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        prefs.edit().putString(KEY_BASE_URL, cleanUrl).apply()
    }

    fun clear() {
        val savedBaseUrl = prefs.getString(KEY_BASE_URL, null)
        prefs.edit().clear().apply()
        // Retain customized base URL across logouts
        if (savedBaseUrl != null) {
            prefs.edit().putString(KEY_BASE_URL, savedBaseUrl).apply()
        }
    }
}
