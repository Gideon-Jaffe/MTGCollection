package com.example.mtgcollection

data class LocationInfo(var locationId: Int?, var locationName: String, var lowPrice: Float?, var highPrice: Float?) {
    override fun toString(): String {
        return locationName
    }

    companion object {
        private const val TAG = "LocationInfo"
    }
}

data class CardsInLocationInfo(var cardId : String, var isCardFoil : Boolean, var locationId: Int, var amount : Int) {

}