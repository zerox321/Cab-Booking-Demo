package com.eramint.locationservice.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eramint.locationservice.local.DataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStore
) : ViewModel() {
    fun setIsCameraMove(value: Boolean) {
        isCameraMoving.value = value
    }

    val locationFlow: Flow<String?> = dataStore.getValue(DataStore.location)
    val isCameraMoving = MutableLiveData<Boolean>(false)
}