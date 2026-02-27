package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val contentHtml: String, // Salva o texto com toda a formatação (RichText)
    val emoji: String,
    val humor: String,
    val date: Long, // Timestamp da criação/edição
    val images: List<String>, // Lista de caminhos das imagens (até 3)
    val audioPath: String?, // Caminho do arquivo de áudio
    val isLocked: Boolean = false, // Se a nota está trancada com o PIN
    val isTimeCapsule: Boolean = false, // Futuro: Cápsula do tempo
    val unlockDate: Long? = null // Data para abrir a cápsula
)
