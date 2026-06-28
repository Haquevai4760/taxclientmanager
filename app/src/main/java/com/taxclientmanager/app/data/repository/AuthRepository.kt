package com.taxclientmanager.app.data.repository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseAuth: Auth
) {
    // বর্তমানে লগইন করা ইউজার কি না তা চেক করার জন্য
    val currentUser: Flow<UserInfo?> = supabaseAuth.sessionStatus.map { _ ->
        supabaseAuth.currentUserOrNull()
    }

    suspend fun signup(email: String, password: String, name: String) {
        supabaseAuth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
            }
        }
    }

    suspend fun login(email: String, password: String) {
        supabaseAuth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        supabaseAuth.signOut()
    }

    suspend fun updateProfile(name: String) {
        supabaseAuth.updateUser {
            data = buildJsonObject {
                put("name", name)
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        // Re-authenticate to verify current password
        val email = supabaseAuth.currentUserOrNull()?.email ?: throw Exception("User not logged in")
        supabaseAuth.signInWith(Email) {
            this.email = email
            this.password = currentPassword
        }
        
        // If sign in succeeds, update password
        supabaseAuth.updateUser {
            password = newPassword
        }
    }

    suspend fun sendResetLink(email: String) {
        supabaseAuth.resetPasswordForEmail(email = email, redirectUrl = "taxclientmanager://reset-password")
    }

    suspend fun updatePassword(newPassword: String) {
        // Ensure session is active before updating to avoid Bearer token error
        if (supabaseAuth.currentSessionOrNull() == null) {
            throw Exception("Session is initializing. Please wait 1-2 seconds and try again.")
        }
        supabaseAuth.updateUser {
            password = newPassword
        }
    }

    fun getSessionStatus() = supabaseAuth.sessionStatus
}
