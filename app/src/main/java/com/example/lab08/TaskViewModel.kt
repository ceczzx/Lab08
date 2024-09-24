package com.example.lab08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    fun addTask(description: String) {
        val newTask = Task(description = description)
        viewModelScope.launch {
            dao.insertTask(newTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTaskById(task.id)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun editTask(task: Task, newDescription: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(description = newDescription)
            dao.updateTask(updatedTask)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun getCompletedTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks().filter { it.isCompleted }
        }
    }

    fun getPendingTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks().filter { !it.isCompleted }
        }
    }

    fun searchTasks(query: String) {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks().filter { it.description.contains(query, ignoreCase = true) }
        }
    }

    fun sortTasksByName() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks().sortedBy { it.description }
        }
    }

    fun sortTasksByCompletion() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks().sortedBy { it.isCompleted }
        }
    }
    fun refreshTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks() // Recarga todas las tareas
        }
    }

}
