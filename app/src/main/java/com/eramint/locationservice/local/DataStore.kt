package com.eramint.locationservice.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DataStore @Inject constructor(private val context: Context, name: String) {

    private val Context.dataStore by preferencesDataStore(name = name)

    suspend fun setValue(value: String, key: String) {
        context.dataStore.edit { preferences -> preferences[stringPreferencesKey(key)] = value }
    }

    suspend fun removeValue(key: String) {
        context.dataStore.edit { preferences -> preferences.remove(stringPreferencesKey(key)) }
    }


    fun getValue(key: String): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[stringPreferencesKey(key)] }

    companion object {
        const val location = "Location"
    }

}