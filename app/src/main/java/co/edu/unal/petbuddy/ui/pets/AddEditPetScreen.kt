package co.edu.unal.petbuddy.ui.pets

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
import co.edu.unal.petbuddy.data.model.Pet
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    navController: NavController,
    petToEdit: Pet?,
    onPetSaved: (Pet) -> Unit
) {
    var petName by remember { mutableStateOf(petToEdit?.name ?: "") }
    var petBreed by remember { mutableStateOf(petToEdit?.breed ?: "") }
    var petAge by remember { mutableStateOf(petToEdit?.age?.toString() ?: "") }
    var petWeight by remember { mutableStateOf(petToEdit?.weight?.toString() ?: "") }
    val isEditing = petToEdit != null
    val screenTitle = if (isEditing) "PetBuddy - Editar Perfil" else "PetBuddy - Crea el perfil de tu amigo"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = petAge, onValueChange = { petAge = it }, label = { Text("Edad (años)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = petWeight, onValueChange = { petWeight = it }, label = { Text("Peso (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val updatedPet = Pet(
                        id = petToEdit?.id ?: UUID.randomUUID().toString(),
                        name = petName,
                        breed = petBreed,
                        age = petAge.toIntOrNull() ?: 0,
                        weight = petWeight.toDoubleOrNull() ?: 0.0
                    )
                    onPetSaved(updatedPet)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Guardar Mascota")
            }
        }
    }
}
