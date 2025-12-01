package co.edu.unal.petbuddy.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "",
    val mood: String = "",
    val appetite: String = "",
    val energyLevel: String = "",
    val notes: String = ""
)
