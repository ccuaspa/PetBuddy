package co.edu.unal.petbuddy

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PetViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _activePet = MutableStateFlow<Pet?>(null)
    val activePet: StateFlow<Pet?> = _activePet

    private val _healthEvents = MutableStateFlow<List<HealthEvent>>(emptyList())
    val healthEvents: StateFlow<List<HealthEvent>> = _healthEvents

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries

    private val _walkEntries = MutableStateFlow<List<WalkEntry>>(emptyList())
    val walkEntries: StateFlow<List<WalkEntry>> = _walkEntries

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    fun loadPets() {
        db.collection("users").document(userId).collection("pets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val petList = it.toObjects(Pet::class.java)
                    _pets.value = petList
                    if (_activePet.value == null || !petList.contains(_activePet.value)) {
                        _activePet.value = petList.firstOrNull()
                    }
                }
            }
    }

    fun savePet(pet: Pet) {
        db.collection("users").document(userId).collection("pets").document(pet.id).set(pet)
    }

    fun deletePet(pet: Pet) {
        db.collection("users").document(userId).collection("pets").document(pet.id).delete()
    }

    fun setActivePet(pet: Pet) {
        _activePet.value = pet
        loadHealthEvents(pet.id)
        loadDiaryEntries(pet.id)
        loadWalkEntries(pet.id)
    }

    private fun loadHealthEvents(petId: String) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("healthEvents")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                snapshot?.let {
                    _healthEvents.value = it.toObjects(HealthEvent::class.java)
                }
            }
    }

    fun saveHealthEvent(petId: String, event: HealthEvent) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("healthEvents").document(event.id).set(event)
    }

    fun deleteHealthEvent(petId: String, event: HealthEvent) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("healthEvents").document(event.id).delete()
    }

    private fun loadDiaryEntries(petId: String) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("diaryEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                snapshot?.let {
                    _diaryEntries.value = it.toObjects(DiaryEntry::class.java)
                }
            }
    }

    fun saveDiaryEntry(petId: String, entry: DiaryEntry) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("diaryEntries").document(entry.id).set(entry)
    }

    fun deleteDiaryEntry(petId: String, entry: DiaryEntry) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("diaryEntries").document(entry.id).delete()
    }

    private fun loadWalkEntries(petId: String) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("walkEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                snapshot?.let {
                    _walkEntries.value = it.toObjects(WalkEntry::class.java)
                }
            }
    }

    fun saveWalkEntry(petId: String, entry: WalkEntry) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("walkEntries").document(entry.id).set(entry)
    }

    fun deleteWalkEntry(petId: String, entry: WalkEntry) {
        db.collection("users").document(userId).collection("pets").document(petId).collection("walkEntries").document(entry.id).delete()
    }
}
