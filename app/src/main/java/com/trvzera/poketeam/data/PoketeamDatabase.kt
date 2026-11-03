package com.trvzera.poketeam.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Usuario::class, MembroEquipe::class], version = 1, exportSchema = true)
abstract class PoketeamDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun equipeDao(): EquipeDao

    companion object {
        @Volatile private var instancia: PoketeamDatabase? = null

        fun getInstance(context: Context): PoketeamDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    PoketeamDatabase::class.java,
                    "poketeam.db"
                ).build().also { instancia = it }
            }
    }
}
