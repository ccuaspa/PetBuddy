package co.edu.unal.petbuddy.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.HealthEvent
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHealthEventScreen(
    navController: NavController,
    eventToEdit: HealthEvent?,
    onSaveEvent: (HealthEvent) -> Unit
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var date by remember { mutableStateOf(eventToEdit?.date ?: "") }
    var type by remember { mutableStateOf(eventToEdit?.type ?: "") }
    val screenTitle = if (eventToEdit != null) "PetBuddy - Editar Evento" else "PetBuddy - Añadir Evento de Salud"

    Scaffold(topBar = { TopAppBar(title = { Text(screenTitle) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }) }) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título del evento") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (DD/MM/AAAA)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (Ej: Vacuna, Consulta)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newEvent = HealthEvent(id = eventToEdit?.id ?: UUID.randomUUID().toString(), title, date, type)
                    onSaveEvent(newEvent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Evento") }
        }
    }
}
