package co.edu.unal.petbuddy.domain

import co.edu.unal.petbuddy.data.model.Pet
import javax.inject.Inject

class GetPetRecommendationsUseCase @Inject constructor() {
    operator fun invoke(pet: Pet): Pair<String, String> {
        val walkRecommendation = when {
            pet.age < 1 -> "4 paseos cortos (15 min)"
            pet.breed.contains("beagle", ignoreCase = true) || pet.breed.contains("border collie", ignoreCase = true) || pet.breed.contains("pastor", ignoreCase = true) -> "3 paseos largos y activos (40 min)"
            pet.weight > 35 -> "2 paseos moderados (30 min)"
            else -> "3 paseos (20 min cada uno)"
        }
        val playRecommendation = when {
            pet.age < 2 -> "1 hora de juego interactivo"
            else -> "45 minutos de juego"
        }
        return Pair(walkRecommendation, playRecommendation)
    }
}
