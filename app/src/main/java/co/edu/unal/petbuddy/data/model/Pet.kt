package co.edu.unal.petbuddy.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0
)
