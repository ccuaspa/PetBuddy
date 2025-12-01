package co.edu.unal.petbuddy

import androidx.lifecycle.ViewModel
import co.edu.unal.petbuddy.data.PetRepository
import co.edu.unal.petbuddy.data.model.*
import co.edu.unal.petbuddy.domain.GetPetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val repository: PetRepository,
    private val getPetRecommendationsUseCase: GetPetRecommendationsUseCase
) : ViewModel() {

    val pets: StateFlow<List<Pet>> = repository.pets
    val activePet: StateFlow<Pet?> = repository.activePet

    val healthEvents: StateFlow<List<HealthEvent>> = repository.healthEvents
    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.diaryEntries
    val walkEntries: StateFlow<List<WalkEntry>> = repository.walkEntries
    val reminders: StateFlow<List<Reminder>> = repository.reminders

    fun getPetRecommendations(pet: Pet): Pair<String, String> {
        return getPetRecommendationsUseCase(pet)
    }

    fun savePet(pet: Pet) {
        repository.savePet(pet)
    }

    fun deletePet(pet: Pet) {
        repository.deletePet(pet)
    }

    fun setActivePet(pet: Pet?) {
        repository.setActivePet(pet)
    }

    fun saveHealthEvent(petId: String, event: HealthEvent) {
        repository.saveHealthEvent(petId, event)
    }

    fun deleteHealthEvent(petId: String, event: HealthEvent) {
        repository.deleteHealthEvent(petId, event)
    }

    fun saveDiaryEntry(petId: String, entry: DiaryEntry) {
        repository.saveDiaryEntry(petId, entry)
    }

    fun deleteDiaryEntry(petId: String, entry: DiaryEntry) {
        repository.deleteDiaryEntry(petId, entry)
    }

    fun saveWalkEntry(petId: String, entry: WalkEntry) {
        repository.saveWalkEntry(petId, entry)
    }

    fun deleteWalkEntry(petId: String, entry: WalkEntry) {
        repository.deleteWalkEntry(petId, entry)
    }

    fun saveReminder(reminder: Reminder) {
        repository.saveReminder(reminder)
    }

    fun deleteReminder(reminder: Reminder) {
        repository.deleteReminder(reminder)
    }
}
