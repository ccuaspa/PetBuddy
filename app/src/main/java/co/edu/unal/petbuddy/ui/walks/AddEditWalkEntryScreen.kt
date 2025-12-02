package co.edu.unal.petbuddy.ui.walks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.WalkEntry
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWalkEntryScreen(
    navController: NavController,
    walkToEdit: WalkEntry?,
    onSaveWalkEntry: (WalkEntry) -> Unit
) {
    var date by remember { mutableStateOf(walkToEdit?.date ?: "") }
    var duration by remember { mutableStateOf(walkToEdit?.duration ?: "") }
    var mood by remember { mutableStateOf(walkToEdit?.mood ?: "") }
    var energyLevel by remember { mutableStateOf(walkToEdit?.energyLevel ?: "") }
    val screenTitle = if (walkToEdit != null) "PetBuddy - Editar Registro" else "PetBuddy - Nuevo Registro de Paseo"

    Scaffold(
        topBar = { TopAppBar(title = { Text(screenTitle) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (DD/MM/AAAA)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duración (minutos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("Ánimo durante el paseo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = energyLevel, onValueChange = { energyLevel = it }, label = { Text("Nivel de Energía") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newEntry = WalkEntry(walkToEdit?.id ?: UUID.randomUUID().toString(), date, duration, mood, energyLevel)
                    onSaveWalkEntry(newEntry)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Registro") }
        }
    }
}
