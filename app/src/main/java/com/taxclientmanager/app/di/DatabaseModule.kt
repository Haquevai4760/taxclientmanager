package com.taxclientmanager.app.di

import android.content.Context
import androidx.room.Room
import com.taxclientmanager.app.data.dao.ClientDao
import com.taxclientmanager.app.data.dao.ReminderDao
import com.taxclientmanager.app.data.dao.ServiceRecordDao
import com.taxclientmanager.app.data.dao.UserDao
import com.taxclientmanager.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao {
        return database.clientDao()
    }

    @Provides
    fun provideServiceRecordDao(database: AppDatabase): ServiceRecordDao {
        return database.serviceRecordDao()
    }

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}

