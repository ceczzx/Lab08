// MainActivity.kt
package com.example.lab08

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lab08.ui.theme.Lab08Theme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.Alignment
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)
            }
        }
    }
}


@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 1.dp, vertical = 25.dp)
    ) {
        // Búsqueda de tareas
        TextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchTasks(searchQuery)
            },
            label = { Text("Buscar tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        // Filtros de tareas completadas o pendientes
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButtonWithText(
                icon = { Icon(Icons.Filled.Task, contentDescription = "Pendientes") },
                text = "Pendientes",
                onClick = { viewModel.getPendingTasks() }
            )
            IconButtonWithText(
                icon = { Icon(Icons.Filled.Check, contentDescription = "Completadas") },
                text = "Completadas",
                onClick = { viewModel.getCompletedTasks() }
            )
            IconButtonWithText(
                icon = { Icon(Icons.Filled.List, contentDescription = "Todas") },
                text = "Todas",
                onClick = { coroutineScope.launch { viewModel.refreshTasks() } }
            )
        }

        // Ordenar tareas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButtonWithText(
                icon = { Icon(Icons.Filled.List, contentDescription = "Ordenar por Nombre") },
                text = "Nombre",
                onClick = { viewModel.sortTasksByName() }
            )
            IconButtonWithText(
                icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Ordenar por Estado") },
                text = "Estado",
                onClick = { viewModel.sortTasksByCompletion() }
            )
        }

        // Formulario para agregar nuevas tareas
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                } else {
                    Toast.makeText(context, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Listado de tareas con funcionalidad de edición y eliminación
        tasks.forEach { task ->
            TaskItem(viewModel, task)
        }
    }
}

@Composable
fun IconButtonWithText(icon: @Composable () -> Unit, text: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            icon()
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TaskItem(viewModel: TaskViewModel, task: Task) {
    var isEditing by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(task.description) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Mostrar la descripción de la tarea
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = task.description, modifier = Modifier.weight(1f))
        }

        // Opciones de edición y eliminación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            if (isEditing) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        viewModel.editTask(task, editedDescription)
                        isEditing = false
                    }) {
                        Text("Guardar")
                    }
                }
            } else {
                Button(onClick = { isEditing = true }) {
                    Text("Editar")
                }
            }
            Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                Text(if (task.isCompleted) "Completada" else "Pendiente")
            }
            Button(onClick = { viewModel.deleteTask(task) }) {
                Text("Eliminar")
            }
        }
    }
}