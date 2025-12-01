package co.edu.unal.petbuddy.data.model

import androidx.annotation.Keep

@Keep
data class Reminder(val id: String = "", val title: String = "", var time: String = "", var enabled: Boolean = false)
