package co.edu.unal.petbuddy.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    navController: NavController,
    reminders: List<Reminder>,
    onUpdateReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Configurar Recordatorios") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_reminder/new") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir recordatorio",
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Activa o desactiva recordatorios para las rutinas de tu mascota.", style = MaterialTheme.typography.bodyMedium) }
            items(reminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onEnabledChange = { isEnabled -> onUpdateReminder(reminder.copy(enabled = isEnabled)) },
                    onEdit = { navController.navigate("add_edit_reminder/${reminder.id}") },
                    onDelete = { onDeleteReminder(reminder) }
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onEnabledChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${reminder.title} (${reminder.time})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = reminder.enabled, onCheckedChange = onEnabledChange)
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                )
            }
        }
    }
}
