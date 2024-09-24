package com.example.lab08

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "priority") val priority: String = "Media", // Prioridad: Alta, Media, Baja
    @ColumnInfo(name = "recurrence_interval") val recurrenceInterval: Int? = null, // Intervalo de recurrencia en días
    @ColumnInfo(name = "category") val category: String? = null // Categoría o etiqueta de la tarea
)