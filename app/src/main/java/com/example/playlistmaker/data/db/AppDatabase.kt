package com.example.playlistmaker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.db.favouriteTracks.FavoriteTrackEntity
import com.example.playlistmaker.data.db.favouriteTracks.FavoriteTracksDao
import com.example.playlistmaker.data.db.playlist.PlaylistDao
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class  // Добавьте эту строку
    ],
    version = 3,  // Увеличьте версию
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao  // Добавьте этот метод

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}