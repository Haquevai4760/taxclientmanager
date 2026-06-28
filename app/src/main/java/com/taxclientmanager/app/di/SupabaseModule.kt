package com.taxclientmanager.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.auth.SettingsSessionManager
import com.russhwolf.settings.Settings
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSettings(): Settings = Settings()

    @Provides
    @Singleton
    fun provideSupabaseClient(settings: Settings): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://pdmibyheaxejlkzmhzrn.supabase.co",
            supabaseKey = "sb_publishable_HLWC7LAVX769NpOleOFpng_ORiIeaC7"
        ) {
            install(Auth) {
                sessionManager = SettingsSessionManager(settings)
            }
            install(Postgrest)
            install(Realtime)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }
}
