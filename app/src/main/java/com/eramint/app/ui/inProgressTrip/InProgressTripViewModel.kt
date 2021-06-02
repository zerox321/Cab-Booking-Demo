package com.eramint.app.ui.inProgressTrip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eramint.app.local.DataStore
import com.eramint.app.util.mapUtility.*
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class InProgressTripViewModel @Inject constructor(
    private val dataStore: DataStore,
    val spherical: Spherical,
    val markerAnimation: MarkerAnimation,
    val mapUtility: MapUtility,
    val directionRepo: DirectionRepo,
    val mapAnimator: MapAnimator,
    val polylineOptions: PolylineOptions
) : ViewModel() {
    val locationFlow: Flow<String?> = dataStore.getValue(DataStore.location)

    val trip = MutableLiveData<InProgressModel>()

    fun updateView(model: InProgressModel) {
        trip.value = model
    }


    val isLocationEnabled = MutableLiveData<Boolean>(true)


}