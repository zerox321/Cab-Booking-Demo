package com.eramint.locationservice.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eramint.locationservice.local.DataStore
import com.eramint.locationservice.ui.HomeActivity.Companion.dropOffViewConstant
import com.eramint.locationservice.ui.HomeActivity.Companion.pickupViewConstant
import com.eramint.locationservice.util.mapUtility.*
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStore,
    val spherical : Spherical,
    val markerAnimation : MarkerAnimation,
    val mapUtility : MapUtility,
    val directionRepo : DirectionRepo,
    val mapAnimator : MapAnimator
) : ViewModel() {
    val locationFlow: Flow<String?> = dataStore.getValue(DataStore.location)

    val viewType = MutableLiveData<Int>(dropOffViewConstant)
    val dropOffPlaceTitleText = MutableLiveData<String>()
    val pickupPlaceTitleText = MutableLiveData<String>()
    fun clearPickupPlaceTitleText() {
        pickupPlaceTitleText.value = ""
    }

    fun setPlaceValue(viewType: Int, location: String) {
        when (viewType) {
            pickupViewConstant -> pickupPlaceTitleText.value = location
            dropOffViewConstant -> dropOffPlaceTitleText.value = location
        }
    }


    val isCameraMoving = MutableLiveData<Boolean>()
    fun setIsCameraMove(value: Boolean) {
        isCameraMoving.value = value
    }

    val isCoveredArea = MutableLiveData<Boolean>()
    fun setIsCoveredArea(value: Boolean) {
        if (value && isCoveredArea.value == null) return
        isCoveredArea.value = value
    }




    val timeValue = MutableLiveData<String>("3")

}