package com.eramint.locationservice.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eramint.locationservice.local.DataStore
import com.eramint.locationservice.ui.HomeActivity.Companion.dropOffViewConstant
import com.eramint.locationservice.ui.HomeActivity.Companion.pickupViewConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStore
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