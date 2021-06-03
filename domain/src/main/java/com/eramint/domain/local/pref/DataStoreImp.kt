package com.eramint.domain.local.pref

object DataStoreImp {

    suspend fun DataStore.saveLocation(value: String) =
        setValue(value = value, key = DataStoreLocation)

    const val DataStoreLocation = "DataStoreLocation"
}