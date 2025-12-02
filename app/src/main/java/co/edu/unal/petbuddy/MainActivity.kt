package co.edu.unal.petbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.unal.petbuddy.ui.dashboard.DashboardScreen
import co.edu.unal.petbuddy.ui.diary.AddEditDiaryEntryScreen
import co.edu.unal.petbuddy.ui.diary.DiaryScreen
import co.edu.unal.petbuddy.ui.health.AddEditHealthEventScreen
import co.edu.unal.petbuddy.ui.health.HealthScreen
import co.edu.unal.petbuddy.ui.login.LoginScreen
import co.edu.unal.petbuddy.ui.pets.AddEditPetScreen
import co.edu.unal.petbuddy.ui.pets.PetsScreen
import co.edu.unal.petbuddy.ui.reminders.AddEditReminderScreen
import co.edu.unal.petbuddy.ui.reminders.RemindersScreen
import co.edu.unal.petbuddy.ui.signup.SignUpScreen
import co.edu.unal.petbuddy.ui.theme.PetBuddyTheme
import co.edu.unal.petbuddy.ui.walks.AddEditWalkEntryScreen
import co.edu.unal.petbuddy.ui.walks.WalkDiaryScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

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
    val reminders by petViewModel.reminders.collectAsState()

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
                            icon = { screen.icon?.let { Icon(imageVector = it, contentDescription = screen.label) } },
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
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    pet = activePet,
                    petsAvailable = pets.isNotEmpty(),
                    petViewModel = petViewModel,
                    onLogout = {
                        auth.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
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

            // --- RUTAS CRUD PARA RECORDATORIOS ---
            composable("reminders_screen") {
                RemindersScreen(
                    navController = navController,
                    reminders = reminders,
                    onUpdateReminder = { reminder -> petViewModel.saveReminder(reminder) },
                    onDeleteReminder = { reminder -> petViewModel.deleteReminder(reminder) }
                )
            }
            composable(
                "add_edit_reminder/{reminderId}",
                arguments = listOf(navArgument("reminderId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getString("reminderId")
                val reminderToEdit = reminders.find { it.id == reminderId }
                AddEditReminderScreen(
                    navController = navController,
                    reminderToEdit = reminderToEdit
                ) { reminder ->
                    petViewModel.saveReminder(reminder)
                    navController.popBackStack()
                }
            }
        }
    }
}
