package co.edu.unal.petbuddy

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A sealed class to define the screens in the app for navigation.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object SignUp : Screen("signup", "SignUp")
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Health : Screen("health", "Salud", Icons.Default.MedicalServices)
    object Diary : Screen("diary", "Diario", Icons.AutoMirrored.Filled.MenuBook)
    object Pets : Screen("pets", "Mascotas", Icons.Default.Pets)
}
