package co.edu.unal.petbuddy.ui.walks

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
import co.edu.unal.petbuddy.data.model.WalkEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkDiaryScreen(
    navController: NavController,
    entries: List<WalkEntry>,
    onDeleteEntry: (WalkEntry) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Diario de Paseos") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_walk_entry/new") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir registro de paseo",
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
            items(entries) { entry ->
                WalkEntryCard(
                    entry = entry,
                    onEdit = { navController.navigate("add_edit_walk_entry/${entry.id}") },
                    onDelete = { onDeleteEntry(entry) }
                )
            }
        }
    }
}

@Composable
fun WalkEntryCard(entry: WalkEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = entry.date, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Duración: ${entry.duration}", style = MaterialTheme.typography.bodyMedium)
                }
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
                Text("Energía: ${entry.energyLevel}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}
