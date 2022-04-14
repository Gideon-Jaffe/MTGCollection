package com.example.mtgcollection

import android.text.Editable

data class LocationInfo(var locationId: Int, var locationName: String, var lowPrice: Float?, var highPrice: Float?) {
    companion object {
        private const val TAG = "LocationInfo"
    }
}