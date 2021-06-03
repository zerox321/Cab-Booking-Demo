package com.eramint.app.ui.bookTrip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eramint.common.mapUtility.*
import com.eramint.data.Constants.dropOffViewConstant
import com.eramint.data.Constants.pickupViewConstant
import com.eramint.domain.local.pref.DataStore
import com.eramint.domain.local.pref.DataStoreImp.DataStoreLocation
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BookTripViewModel @Inject constructor(
    private val dataStore: DataStore,
    val spherical: Spherical,
    val markerAnimation: MarkerAnimation,
    val mapUtility: MapUtility,
    val directionRepo: DirectionRepo,
    val mapAnimator: MapAnimator,
    val polylineOptions: PolylineOptions
) : ViewModel() {
    val locationFlow: Flow<String?> =
        dataStore.getValue(DataStoreLocation)

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

    val isLocationEnabled = MutableLiveData<Boolean>(true)


    val isSelected = MutableLiveData<Boolean>()
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