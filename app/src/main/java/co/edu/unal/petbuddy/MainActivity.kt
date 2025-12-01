package co.edu.unal.petbuddy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.unal.petbuddy.data.model.*
import co.edu.unal.petbuddy.ui.theme.PetBuddyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

// sealed class para definir las pantallas principales de nuestra app
sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "SignUp")
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Health : Screen("health", "Salud", Icons.Default.Favorite)
    object Diary : Screen("diary", "Diario", Icons.Default.Edit)
    object Pets : Screen("pets", "Mascotas", Icons.AutoMirrored.Filled.List)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            PetBuddyTheme {
                PetBuddyApp(auth = auth)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetBuddyApp(auth: FirebaseAuth, petViewModel: PetViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentUser = auth.currentUser

    val pets by petViewModel.pets.collectAsState()
    val activePet by petViewModel.activePet.collectAsState()
    val healthEvents by petViewModel.healthEvents.collectAsState()
    val diaryEntries by petViewModel.diaryEntries.collectAsState()
    val walkEntries by petViewModel.walkEntries.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(Screen.Dashboard, Screen.Health, Screen.Diary, Screen.Pets)
    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    val startDestination = if (currentUser != null) {
        Screen.Dashboard.route // Always start at dashboard if logged in
    } else {
        Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
            // --- AUTH SCREENS ---
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    auth = auth,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    navController = navController,
                    auth = auth,
                    onSignUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- MAIN APP SCREENS ---
            composable(Screen.Dashboard.route) { DashboardScreen(navController = navController, pet = activePet, petsAvailable = pets.isNotEmpty(), petViewModel = petViewModel) }
            composable(Screen.Health.route) {
                HealthScreen(
                    navController = navController,
                    events = healthEvents,
                    onDeleteEvent = { event ->
                        activePet?.let { petViewModel.deleteHealthEvent(it.id, event) }
                    }
                )
            }
            composable(Screen.Diary.route) {
                DiaryScreen(
                    navController = navController,
                    entries = diaryEntries,
                    onDeleteEntry = { entry ->
                        activePet?.let { petViewModel.deleteDiaryEntry(it.id, entry) }
                    }
                )
            }
            composable(Screen.Pets.route) {
                PetsScreen(
                    navController = navController,
                    pets = pets,
                    activePet = activePet,
                    onSetAsActive = { pet -> petViewModel.setActivePet(pet) },
                    onDeletePet = { pet -> petViewModel.deletePet(pet) }
                )
            }

            // --- RUTAS CRUD PARA MASCOTAS ---
            composable(
                route = "add_edit_pet_screen/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                val petToEdit = pets.find { it.id == petId }
                AddEditPetScreen(
                    navController = navController,
                    petToEdit = petToEdit,
                    onPetSaved = { pet ->
                        petViewModel.savePet(pet)
                        navController.popBackStack()
                    }
                )
            }

            // --- RUTAS CRUD PARA SALUD ---
            composable(
                "add_edit_health_event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                val eventToEdit = healthEvents.find { it.id == eventId }
                AddEditHealthEventScreen(
                    navController = navController,
                    eventToEdit = eventToEdit
                ) { event ->
                    activePet?.let { petViewModel.saveHealthEvent(it.id, event) }
                    navController.popBackStack()
                }
            }

            // --- RUTAS CRUD PARA DIARIO DE COMPORTAMIENTO ---
            composable(
                "add_edit_diary_entry/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")
                val entryToEdit = diaryEntries.find { it.id == entryId }
                AddEditDiaryEntryScreen(
                    navController = navController,
                    entryToEdit = entryToEdit
                ) { entry ->
                    activePet?.let { petViewModel.saveDiaryEntry(it.id, entry) }
                    navController.popBackStack()
                }
            }

            // --- RUTAS CRUD PARA DIARIO DE PASEOS ---
            composable("walk_diary_screen") {
                WalkDiaryScreen(
                    navController = navController,
                    entries = walkEntries,
                    onDeleteEntry = { entry ->
                        activePet?.let { petViewModel.deleteWalkEntry(it.id, entry) }
                    }
                )
            }
            composable(
                "add_edit_walk_entry/{walkId}",
                arguments = listOf(navArgument("walkId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val walkId = backStackEntry.arguments?.getString("walkId")
                val walkToEdit = walkEntries.find { it.id == walkId }
                AddEditWalkEntryScreen(
                    navController = navController,
                    walkToEdit = walkToEdit
                ) { walk ->
                    activePet?.let { petViewModel.saveWalkEntry(it.id, walk) }
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@Composable
fun SignUpScreen(
    navController: NavController,
    auth: FirebaseAuth,
    onSignUpSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSignUpSuccess()
                            } else {
                                Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text("Already have an account? Login")
        }
    }
}


// --- PANTALLA DE SALUD Y COMPONENTES (CRUD) ---

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
                Icon(Icons.Default.Add, contentDescription = "Añadir evento de salud")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
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
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar") }
        }
    }
}

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

    Scaffold(topBar = { TopAppBar(title = { Text(screenTitle) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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


// --- PANTALLA DE DIARIO Y COMPONENTES (CRUD) ---

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
                Icon(Icons.Default.Add, contentDescription = "Añadir entrada al diario")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedButton(onClick = { navController.navigate("walk_diary_screen") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
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
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar") }
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text("Ánimo: ${entry.mood}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("Apetito: ${entry.appetite}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            Text("Nivel de Energía: ${entry.energyLevel}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            if (entry.notes.isNotBlank()) {
                Text(text = entry.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

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
        topBar = { TopAppBar(title = { Text(screenTitle) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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


// --- PANTALLAS DE PASEOS Y COMPONENTES (CRUD) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkDiaryScreen(
    navController: NavController,
    entries: List<WalkEntry>,
    onDeleteEntry: (WalkEntry) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("PetBuddy - Diario de Paseos") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit_walk_entry/new") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir registro de paseo")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar") }
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text("Ánimo: ${entry.mood}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("Energía: ${entry.energyLevel}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

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
        topBar = { TopAppBar(title = { Text(screenTitle) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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


// --- OTRAS PANTALLAS ---

@Composable
fun DashboardScreen(navController: NavController, pet: Pet?, petsAvailable: Boolean, petViewModel: PetViewModel) {
    if (pet == null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (petsAvailable) "No hay ninguna mascota activa." else "No tienes mascotas registradas.",
                    style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { navController.navigate(Screen.Pets.route) }) {
                    Text(if (petsAvailable) "Seleccionar una mascota" else "Añadir una mascota")
                }
            }
        }
        return
    }

    val (walkRecommendation, playRecommendation) = petViewModel.getPetRecommendations(pet)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("¡Hola, ${pet.name}!", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 8.dp)) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Actividad Diaria", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Paseos recomendados: $walkRecommendation")
                    Text(text = "Tiempo de juego: $playRecommendation")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Bienestar Emocional", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Registra el ánimo de tu mascota hoy.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigate(Screen.Diary.route) }, modifier = Modifier.align(Alignment.End)) { Text("Registrar") }
                }
            }
        }
    }
}

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
                Icon(Icons.Default.Add, contentDescription = "Añadir nueva mascota")
            }
        }
    ) { innerPadding ->
        if (pets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no has añadido ninguna mascota. \nToca el botón (+) para empezar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                if (!isActive) {
                    Button(onClick = onSetAsActive) { Text("Activar") }
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
            }
        }
    }
}

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
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
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(text = "Guardar Mascota")
            }
        }
    }
}
