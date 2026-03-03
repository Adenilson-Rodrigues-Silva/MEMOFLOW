package com.example.memoflow

import android.app.Application
import androidx.room.Room
import com.example.memoflow.data.local.MemoDatabase
import com.example.memoflow.data.repository.MemoRepository

class MemoApplication : Application() {
    
    // Lazy inicialização do banco de dados
    val database by lazy { 
        Room.databaseBuilder(
            this,
            MemoDatabase::class.java,
            "memo_flow_db"
        )
        .fallbackToDestructiveMigration() // Se mudar o banco, ele reseta (bom para desenvolvimento)
        .build() 
    }

    val repository by lazy { 
        MemoRepository(database.noteDao(), database.userDao(), database.gratitudeDao())
    }
}
