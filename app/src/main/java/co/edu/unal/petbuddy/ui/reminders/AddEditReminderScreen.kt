package co.edu.unal.petbuddy.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.Reminder
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    navController: NavController,
    reminderToEdit: Reminder?,
    onSaveReminder: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminderToEdit?.title ?: "") }
    var time by remember { mutableStateOf(reminderToEdit?.time ?: "") }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (reminderToEdit != null) "Editar Recordatorio" else "Nuevo Recordatorio") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Hora (HH:mm)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newReminder = Reminder(id = reminderToEdit?.id ?: UUID.randomUUID().toString(), title = title, time = time, enabled = reminderToEdit?.enabled ?: true)
                    onSaveReminder(newReminder)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}
