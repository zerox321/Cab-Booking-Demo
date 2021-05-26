package com.eramint.locationservice.local

object DataStoreImp {

    suspend fun DataStore.saveLocation(value: String) =
        setValue(value = value, key = DataStore.location)
}