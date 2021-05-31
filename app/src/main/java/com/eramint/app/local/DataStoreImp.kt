package com.eramint.app.local

object DataStoreImp {

    suspend fun DataStore.saveLocation(value: String) =
        setValue(value = value, key = DataStore.location)

}