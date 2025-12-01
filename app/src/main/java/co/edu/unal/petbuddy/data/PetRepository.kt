package co.edu.unal.petbuddy.data

import android.util.Log
import co.edu.unal.petbuddy.data.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor() {
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

    private var petsListener: ListenerRegistration? = null
    private var healthListener: ListenerRegistration? = null
    private var diaryListener: ListenerRegistration? = null
    private var walkListener: ListenerRegistration? = null

    private val userId: String?
        get() = auth.currentUser?.uid

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadPets()
            } else {
                // Clear all data and listeners if user logs out
                petsListener?.remove()
                healthListener?.remove()
                diaryListener?.remove()
                walkListener?.remove()
                _pets.value = emptyList()
                _activePet.value = null
                _healthEvents.value = emptyList()
                _diaryEntries.value = emptyList()
                _walkEntries.value = emptyList()
            }
        }
    }

    private fun loadPets() {
        val uid = userId ?: return
        petsListener = db.collection("users").document(uid).collection("pets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val petList = it.toObjects(Pet::class.java)
                    _pets.value = petList
                    // If active pet is gone or was never set, set it to the first pet
                    if (_activePet.value == null || !_pets.value.contains(_activePet.value)) {
                        setActivePet(petList.firstOrNull())
                    }
                }
            }
    }

    fun savePet(pet: Pet) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(pet.id).set(pet)
    }

    fun deletePet(pet: Pet) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(pet.id).delete()
    }

    fun setActivePet(pet: Pet?) {
        _activePet.value = pet
        // remove old listeners
        healthListener?.remove()
        diaryListener?.remove()
        walkListener?.remove()

        _healthEvents.value = emptyList()
        _diaryEntries.value = emptyList()
        _walkEntries.value = emptyList()

        // if there's a new active pet, attach new listeners
        pet?.let {
            loadHealthEvents(it.id)
            loadDiaryEntries(it.id)
            loadWalkEntries(it.id)
        }
    }

    private fun loadHealthEvents(petId: String) {
        val uid = userId ?: return
        healthListener = db.collection("users").document(uid).collection("pets").document(petId).collection("healthEvents")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _healthEvents.value = it.toObjects(HealthEvent::class.java) }
            }
    }

    fun saveHealthEvent(petId: String, event: HealthEvent) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("healthEvents").document(event.id).set(event)
    }

    fun deleteHealthEvent(petId: String, event: HealthEvent) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("healthEvents").document(event.id).delete()
    }

    private fun loadDiaryEntries(petId: String) {
        val uid = userId ?: return
        diaryListener = db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _diaryEntries.value = it.toObjects(DiaryEntry::class.java) }
            }
    }

    fun saveDiaryEntry(petId: String, entry: DiaryEntry) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries").document(entry.id).set(entry)
    }

    fun deleteDiaryEntry(petId: String, entry: DiaryEntry) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries").document(entry.id).delete()
    }

    private fun loadWalkEntries(petId: String) {
        val uid = userId ?: return
        walkListener = db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _walkEntries.value = it.toObjects(WalkEntry::class.java) }
            }
    }

    fun saveWalkEntry(petId: String, entry: WalkEntry) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries").document(entry.id).set(entry)
    }

    fun deleteWalkEntry(petId: String, entry: WalkEntry) {
        val uid = userId ?: return
        db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries").document(entry.id).delete()
    }
}
