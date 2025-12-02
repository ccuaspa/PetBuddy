package co.edu.unal.petbuddy.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.unal.petbuddy.PetViewModel
import co.edu.unal.petbuddy.Screen
import co.edu.unal.petbuddy.data.model.Pet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    pet: Pet?,
    petsAvailable: Boolean,
    petViewModel: PetViewModel,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetBuddy") },
                actions = {
                    IconButton(onClick = { navController.navigate("reminders_screen") }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Recordatorios")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pet == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (petsAvailable) "No hay ninguna mascota activa." else "No tienes mascotas registradas.",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { navController.navigate(Screen.Pets.route) }) {
                        Text(if (petsAvailable) "Seleccionar una mascota" else "Añadir una mascota")
                    }
                }
            }
            return@Scaffold
        }

        val (walkRecommendation, playRecommendation) = petViewModel.getPetRecommendations(pet)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "¡Hola, ${pet.name}!",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Actividad Diaria", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Paseos recomendados: $walkRecommendation")
                        Text(text = "Tiempo de juego: $playRecommendation")
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bienestar Emocional",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Registra el ánimo de tu mascota hoy.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Diary.route) },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Registrar") }
                    }
                }
            }
        }
    }
}
