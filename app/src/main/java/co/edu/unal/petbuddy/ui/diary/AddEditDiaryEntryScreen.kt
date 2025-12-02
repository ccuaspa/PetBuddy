package co.edu.unal.petbuddy.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.DiaryEntry
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDiaryEntryScreen(
    navController: NavController,
    entryToEdit: DiaryEntry?,
    onSaveEntry: (DiaryEntry) -> Unit
) {
    var date by remember { mutableStateOf(entryToEdit?.date ?: "") }
    var mood by remember { mutableStateOf(entryToEdit?.mood ?: "") }
    var appetite by remember { mutableStateOf(entryToEdit?.appetite ?: "") }
    var energyLevel by remember { mutableStateOf(entryToEdit?.energyLevel ?: "") }
    var notes by remember { mutableStateOf(entryToEdit?.notes ?: "") }
    val screenTitle = if (entryToEdit != null) "PetBuddy - Editar Entrada" else "PetBuddy - Nueva Entrada del Diario"

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
            OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("Ánimo (Ej: Feliz, Cansado)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = appetite, onValueChange = { appetite = it }, label = { Text("Apetito (Ej: Normal, Bajo)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = energyLevel, onValueChange = { energyLevel = it }, label = { Text("Nivel de Energía (Ej: Alto, Bajo)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas adicionales") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newEntry = DiaryEntry(entryToEdit?.id ?: UUID.randomUUID().toString(), date, mood, appetite, energyLevel, notes)
                    onSaveEntry(newEntry)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar Entrada") }
        }
    }
}
