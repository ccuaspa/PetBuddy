package co.edu.unal.petbuddy.ui.diary

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
import co.edu.unal.petbuddy.data.model.DiaryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    navController: NavController,
    entries: List<DiaryEntry>,
    onDeleteEntry: (DiaryEntry) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Diario de Comportamiento") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_diary_entry/new") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir entrada al diario",
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)) {
            OutlinedButton(
                onClick = { navController.navigate("walk_diary_screen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Ver/Añadir Registros de Paseos")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(entries) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onEdit = { navController.navigate("add_edit_diary_entry/${entry.id}") },
                        onDelete = { onDeleteEntry(entry) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryEntryCard(entry: DiaryEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = entry.date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
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
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text("Ánimo: ${entry.mood}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("Apetito: ${entry.appetite}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            Text(
                "Nivel de Energía: ${entry.energyLevel}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (entry.notes.isNotBlank()) {
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
