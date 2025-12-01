package co.edu.unal.petbuddy.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class HealthEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: String = "",
    val type: String = ""
)
