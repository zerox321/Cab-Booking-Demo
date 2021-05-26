package com.eramint.locationservice.ui.home

import androidx.lifecycle.ViewModel
import com.eramint.locationservice.local.DataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStore
) : ViewModel() {
    val locationFlow: Flow<String?> = dataStore.getValue(DataStore.location)
}