package co.edu.unal.petbuddy.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.HealthEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    navController: NavController,
    events: List<HealthEvent>,
    onDeleteEvent: (HealthEvent) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Historial de Salud") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_health_event/new") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evento de salud",
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                HealthEventCard(
                    event = event,
                    onEdit = { navController.navigate("add_edit_health_event/${event.id}") },
                    onDelete = { onDeleteEvent(event) }
                )
            }
        }
    }
}

@Composable
fun HealthEventCard(event: HealthEvent, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                Text(text = event.date, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = event.type, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp))
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
