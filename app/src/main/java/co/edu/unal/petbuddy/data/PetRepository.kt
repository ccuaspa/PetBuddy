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

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    private var petsListener: ListenerRegistration? = null
    private var healthListener: ListenerRegistration? = null
    private var diaryListener: ListenerRegistration? = null
    private var walkListener: ListenerRegistration? = null
    private var remindersListener: ListenerRegistration? = null

    private val userId: String?
        get() = auth.currentUser?.uid

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                loadPets()
                loadReminders()
            } else {
                petsListener?.remove()
                healthListener?.remove()
                diaryListener?.remove()
                walkListener?.remove()
                remindersListener?.remove()
                _pets.value = emptyList()
                _activePet.value = null
                _healthEvents.value = emptyList()
                _diaryEntries.value = emptyList()
                _walkEntries.value = emptyList()
                _reminders.value = emptyList()
            }
        }
    }

    private fun loadPets() {
        val uid = userId ?: return
        petsListener = db.collection("users").document(uid).collection("pets")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Pets listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val petList = it.toObjects(Pet::class.java)
                    _pets.value = petList
                    if (_activePet.value == null || !_pets.value.contains(_activePet.value)) {
                        setActivePet(petList.firstOrNull())
                    }
                }
            }
    }

    fun savePet(pet: Pet) {
        val uid = userId ?: return
        // Optimistic update
        val currentPets = _pets.value.toMutableList()
        val index = currentPets.indexOfFirst { it.id == pet.id }
        if (index != -1) {
            currentPets[index] = pet
        } else {
            currentPets.add(pet)
        }
        _pets.value = currentPets

        db.collection("users").document(uid).collection("pets").document(pet.id).set(pet)
            .addOnFailureListener { e -> Log.e("PetRepository", "Error saving pet", e) }
    }

    fun deletePet(pet: Pet) {
        val uid = userId ?: return
        // Optimistic update
        _pets.value = _pets.value.filterNot { it.id == pet.id }

        db.collection("users").document(uid).collection("pets").document(pet.id).delete()
            .addOnFailureListener { e -> Log.e("PetRepository", "Error deleting pet", e) }
    }

    fun setActivePet(pet: Pet?) {
        _activePet.value = pet
        healthListener?.remove()
        diaryListener?.remove()
        walkListener?.remove()
        _healthEvents.value = emptyList()
        _diaryEntries.value = emptyList()
        _walkEntries.value = emptyList()
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
                    Log.w("PetRepository", "Health events listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _healthEvents.value = it.toObjects(HealthEvent::class.java) }
            }
    }

    fun saveHealthEvent(petId: String, event: HealthEvent) {
        val uid = userId ?: return
        // Optimistic update
        val currentEvents = _healthEvents.value.toMutableList()
        val index = currentEvents.indexOfFirst { it.id == event.id }
        if (index != -1) {
            currentEvents[index] = event
        } else {
            currentEvents.add(event)
        }
        _healthEvents.value = currentEvents

        db.collection("users").document(uid).collection("pets").document(petId).collection("healthEvents").document(event.id).set(event)
            .addOnFailureListener { e -> Log.e("PetRepository", "Error saving health event", e) }
    }

    fun deleteHealthEvent(petId: String, event: HealthEvent) {
        val uid = userId ?: return
        // Optimistic update
        _healthEvents.value = _healthEvents.value.filterNot { it.id == event.id }

        db.collection("users").document(uid).collection("pets").document(petId).collection("healthEvents").document(event.id).delete()
            .addOnFailureListener { e -> Log.e("PetRepository", "Error deleting health event", e) }
    }

    private fun loadDiaryEntries(petId: String) {
        val uid = userId ?: return
        diaryListener = db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Diary entries listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _diaryEntries.value = it.toObjects(DiaryEntry::class.java) }
            }
    }

    fun saveDiaryEntry(petId: String, entry: DiaryEntry) {
        val uid = userId ?: return
        // Optimistic update
        val currentEntries = _diaryEntries.value.toMutableList()
        val index = currentEntries.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            currentEntries[index] = entry
        } else {
            currentEntries.add(entry)
        }
        _diaryEntries.value = currentEntries

        db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries").document(entry.id).set(entry)
            .addOnFailureListener { e -> Log.e("PetRepository", "Error saving diary entry", e) }
    }

    fun deleteDiaryEntry(petId: String, entry: DiaryEntry) {
        val uid = userId ?: return
        // Optimistic update
        _diaryEntries.value = _diaryEntries.value.filterNot { it.id == entry.id }

        db.collection("users").document(uid).collection("pets").document(petId).collection("diaryEntries").document(entry.id).delete()
            .addOnFailureListener { e -> Log.e("PetRepository", "Error deleting diary entry", e) }
    }

    private fun loadWalkEntries(petId: String) {
        val uid = userId ?: return
        walkListener = db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Walk entries listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _walkEntries.value = it.toObjects(WalkEntry::class.java) }
            }
    }

    fun saveWalkEntry(petId: String, entry: WalkEntry) {
        val uid = userId ?: return
        // Optimistic update
        val currentEntries = _walkEntries.value.toMutableList()
        val index = currentEntries.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            currentEntries[index] = entry
        } else {
            currentEntries.add(entry)
        }
        _walkEntries.value = currentEntries

        db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries").document(entry.id).set(entry)
            .addOnFailureListener { e -> Log.e("PetRepository", "Error saving walk entry", e) }
    }

    fun deleteWalkEntry(petId: String, entry: WalkEntry) {
        val uid = userId ?: return
        // Optimistic update
        _walkEntries.value = _walkEntries.value.filterNot { it.id == entry.id }

        db.collection("users").document(uid).collection("pets").document(petId).collection("walkEntries").document(entry.id).delete()
            .addOnFailureListener { e -> Log.e("PetRepository", "Error deleting walk entry", e) }
    }

    private fun loadReminders() {
        val uid = userId ?: return
        remindersListener = db.collection("users").document(uid).collection("reminders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PetRepository", "Reminders listen failed.", e)
                    return@addSnapshotListener
                }
                snapshot?.let { _reminders.value = it.toObjects(Reminder::class.java) }
            }
    }

    fun saveReminder(reminder: Reminder) {
        val uid = userId ?: return
        // Optimistic update
        val currentReminders = _reminders.value.toMutableList()
        val index = currentReminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            currentReminders[index] = reminder
        } else {
            currentReminders.add(reminder)
        }
        _reminders.value = currentReminders

        db.collection("users").document(uid).collection("reminders").document(reminder.id).set(reminder)
            .addOnFailureListener { e -> Log.e("PetRepository", "Error saving reminder", e) }
    }

    fun deleteReminder(reminder: Reminder) {
        val uid = userId ?: return
        // Optimistic update
        _reminders.value = _reminders.value.filterNot { it.id == reminder.id }

        db.collection("users").document(uid).collection("reminders").document(reminder.id).delete()
            .addOnFailureListener { e -> Log.e("PetRepository", "Error deleting reminder", e) }
    }
}
