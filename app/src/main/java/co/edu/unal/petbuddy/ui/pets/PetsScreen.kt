package co.edu.unal.petbuddy.ui.pets

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.data.model.Pet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    navController: NavController,
    pets: List<Pet>,
    activePet: Pet?,
    onSetAsActive: (Pet) -> Unit,
    onDeletePet: (Pet) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Mis Mascotas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_pet_screen/new") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir nueva mascota",
                )
            }
        }
    ) { innerPadding ->
        if (pets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no has añadido ninguna mascota. \nToca el botón (+) para empezar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pets) { pet ->
                    PetItemCard(
                        pet = pet,
                        isActive = pet.id == activePet?.id,
                        onSetAsActive = { onSetAsActive(pet) },
                        onEdit = { navController.navigate("add_edit_pet_screen/${pet.id}") },
                        onDelete = { onDeletePet(pet) }
                    )
                }
            }
        }
    }
}

@Composable
fun PetItemCard(
    pet: Pet,
    isActive: Boolean,
    onSetAsActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isActive) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pet.name, style = MaterialTheme.typography.titleLarge)
            Text("${pet.breed} - ${pet.age} años, ${pet.weight} kg", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                if (!isActive) {
                    Button(onClick = onSetAsActive) { Text("Activar") }
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
        }
    }
}
