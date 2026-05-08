package com.arsdevstudio.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.MapPrefs
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class MapFilterPeriod(val label: String) {
    LAST_MONTH("30 dias"),
    SIX_MONTHS("6 meses"),
    ONE_YEAR("1 ano"),
    ALL("Tudo")
}

data class MapUiState(
    val notesWithLocation: List<NoteEntity> = emptyList(),
    val selectedNotes: List<NoteEntity> = emptyList(),
    val currentFilter: MapFilterPeriod = MapFilterPeriod.SIX_MONTHS,
    val isLoading: Boolean = false,
    val isFiltering: Boolean = false,
    val initialLocation: Triple<Double, Double, Float>? = null
)

class MapViewModel(
    private val repository: MemoRepository,
    private val mapPrefs: MapPrefs
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(MapFilterPeriod.SIX_MONTHS)
    private val _isLoading = MutableStateFlow(false)
    private val _isFiltering = MutableStateFlow(false)
    private val _selectedNotes = MutableStateFlow<List<NoteEntity>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MapUiState> = combine(
        repository.userSettings.flatMapLatest { user ->
            val userId = if (user?.isGoogleLogged == true) user.firebaseUid ?: "" else ""
            if (userId.isNotEmpty()) {
                _currentFilter.flatMapLatest { filter ->
                    repository.getNotesWithLocationSince(userId, getSinceDate(filter))
                }
            } else {
                flowOf(emptyList())
            }
        },
        _selectedNotes,
        _currentFilter,
        _isLoading,
        _isFiltering,
        mapPrefs.lastLocation
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        MapUiState(
            notesWithLocation = args[0] as List<NoteEntity>,
            selectedNotes = args[1] as List<NoteEntity>,
            currentFilter = args[2] as MapFilterPeriod,
            isLoading = args[3] as Boolean,
            isFiltering = args[4] as Boolean,
            initialLocation = args[5] as Triple<Double, Double, Float>?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MapUiState())

    fun setFilter(filter: MapFilterPeriod) {
        viewModelScope.launch {
            _isFiltering.value = true
            _currentFilter.value = filter
            delay(3500) 
            _isFiltering.value = false
        }
    }

    fun onMapLongClick(latLng: LatLng) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.userSettings.first()
            val userId = if (user?.isGoogleLogged == true) user.firebaseUid ?: "" else ""
            if (userId.isEmpty()) {
                _isLoading.value = false
                return@launch
            }
            val threshold = 0.0015
            repository.getNotesByLocationAreaFiltered(
                userId = userId,
                minLat = latLng.latitude - threshold,
                maxLat = latLng.latitude + threshold,
                minLon = latLng.longitude - threshold,
                maxLon = latLng.longitude + threshold,
                sinceDate = getSinceDate(_currentFilter.value)
            ).first().let { notes ->
                _selectedNotes.value = notes
            }
            _isLoading.value = false
        }
    }

    fun saveLastLocation(lat: Double, lng: Double, zoom: Float) {
        viewModelScope.launch {
            mapPrefs.saveLocation(lat, lng, zoom)
        }
    }

    fun clearSelectedNotes() {
        _selectedNotes.value = emptyList()
    }

    private fun getSinceDate(filter: MapFilterPeriod): Long {
        val calendar = Calendar.getInstance()
        when (filter) {
            MapFilterPeriod.LAST_MONTH -> calendar.add(Calendar.MONTH, -1)
            MapFilterPeriod.SIX_MONTHS -> calendar.add(Calendar.MONTH, -6)
            MapFilterPeriod.ONE_YEAR -> calendar.add(Calendar.YEAR, -1)
            MapFilterPeriod.ALL -> return 0L
        }
        return calendar.timeInMillis
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.arsdevstudio.memoflow.MemoApplication
                return MapViewModel(
                    application.repository,
                    MapPrefs(application.applicationContext)
                ) as T
            }
        }
    }
}

