package co.edu.unal.petbuddy.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class WalkEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val duration: String = "",
    val mood: String = "",
    val energyLevel: String = ""
)
