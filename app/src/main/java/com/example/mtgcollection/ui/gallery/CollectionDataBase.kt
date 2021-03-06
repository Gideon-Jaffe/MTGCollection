package com.example.mtgcollection.ui.gallery

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mtgcollection.CardsInLocationInfo
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.Prices
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CollectionDBHelper (context: Context) : SQLiteOpenHelper(context, "MyCollection", null, 1) {
    private var sqLiteDatabase: SQLiteDatabase = this.writableDatabase

    enum class Ordering {
        NONE, NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableStatement = "CREATE TABLE $COLLECTION_TABLE (" +
                "$COLUMN_ID TEXT, " +
                "$COLUMN_CARD_NAME TEXT, " +
                "$COLUMN_CARD_SET TEXT, " +
                "$COLUMN_IS_FOIL INTEGER, " +
                "$COLUMN_RARITY CHAR, " +
                "$COLUMN_AMOUNT INTEGER, " +
                "$COLUMN_USD REAL, " +
                "$COLUMN_USD_FOIL REAL, " +
                "$COLUMN_EUR REAL, " +
                "$COLUMN_EUR_FOIL REAL, " +
                "$COLUMN_TIX REAL, " +
                "$COLUMN_TIX_FOIL REAL, " +
                "$COLUMN_PRICE_LAST_UPDATED TEXT, " +
                "PRIMARY KEY ($COLUMN_ID, $COLUMN_IS_FOIL))"

        val createLocationStatement = "CREATE TABLE $LOCATION_TABLE (" +
                "$COLUMN_LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_LOCATION_NAME TEXT, " +
                "$COLUMN_LOW_PRICE REAL, " +
                "$COLUMN_HIGH_PRICE REAL)"

        val createCardInLocationTableStatement = "CREATE TABLE $CARD_IN_LOCATION_TABLE (" +
                "$COLUMN_ID TEXT, " +
                "$COLUMN_IS_FOIL INTEGER, " +
                "$COLUMN_LOCATION_ID INTEGER, " +
                "$COLUMN_AMOUNT INTEGER, " +
                "PRIMARY KEY ($COLUMN_ID, $COLUMN_IS_FOIL, $COLUMN_LOCATION_ID))"

        db?.execSQL(createTableStatement)
        db?.execSQL(createLocationStatement)
        db?.execSQL(createCardInLocationTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    override fun close() {
        sqLiteDatabase.close()
        super.close()
    }

    fun addOne(card_info : MTGCardInfo, locationId : Int? = null) : Boolean
    {
        if (getOneCard(card_info) != null) {
            updateAmountOfCardInLocation(card_info, card_info.amount, locationId)
            return updateAmount(card_info, true)
        } else {
            val contentValues = ContentValues()

            contentValues.put(COLUMN_ID, card_info.id)
            contentValues.put(COLUMN_CARD_NAME, card_info.card_name)
            contentValues.put(COLUMN_CARD_SET, card_info.set)
            contentValues.put(COLUMN_IS_FOIL, card_info.isFoil)
            contentValues.put(COLUMN_RARITY, card_info.rarity)
            contentValues.put(COLUMN_AMOUNT, card_info.amount)
            contentValues.put(COLUMN_USD, card_info.prices.usd)
            contentValues.put(COLUMN_USD_FOIL, card_info.prices.usd_foil)
            contentValues.put(COLUMN_EUR, card_info.prices.eur)
            contentValues.put(COLUMN_EUR_FOIL, card_info.prices.eur_foil)
            contentValues.put(COLUMN_TIX, card_info.prices.tix)
            contentValues.put(COLUMN_TIX_FOIL, card_info.prices.tix_foil)
            contentValues.put(COLUMN_PRICE_LAST_UPDATED, SimpleDateFormat("yyyy-MM-dd HH:mm:SS.SSS", Locale.US).format(Calendar.getInstance().time))

            val insert = sqLiteDatabase.insert(COLLECTION_TABLE, null, contentValues)
            updateAmountOfCardInLocation(card_info, card_info.amount, locationId)
            return -1L != insert
        }
    }

    private fun updateAmountOfCardInLocation(card_info : MTGCardInfo, amount : Int = 1, locationId : Int? = null) : Boolean {
        val cardsInLocationInfo : CardsInLocationInfo? = getCardInLocation(card_info.id, card_info.isFoil, locationId)
        val contentValues = ContentValues()

        if (cardsInLocationInfo == null) {
            contentValues.put(COLUMN_ID, card_info.id)
            contentValues.put(COLUMN_IS_FOIL, card_info.isFoil)
            contentValues.put(COLUMN_LOCATION_ID, locationId)
            contentValues.put(COLUMN_AMOUNT, amount)

            val insert = sqLiteDatabase.insert(CARD_IN_LOCATION_TABLE, null, contentValues)
            return -1L != insert
        } else {
            if (cardsInLocationInfo.amount + amount < 1) {
                removeCardInLocation(card_info.id, card_info.isFoil, locationId)
            } else {
                contentValues.put(COLUMN_AMOUNT, cardsInLocationInfo.amount + amount)
                val update = sqLiteDatabase.update(
                    CARD_IN_LOCATION_TABLE, contentValues, "$COLUMN_ID=? AND $COLUMN_IS_FOIL=? AND $COLUMN_LOCATION_ID=?",
                    arrayOf(cardsInLocationInfo.cardId, cardsInLocationInfo.isCardFoil.toString(), cardsInLocationInfo.locationId.toString())
                )
                return update == 1
            }
        }
        return false
    }

    fun addLocation(location : LocationInfo) : Boolean {
        val contentValues = ContentValues()

        contentValues.put(COLUMN_LOCATION_NAME, location.locationName)
        contentValues.put(COLUMN_LOW_PRICE, location.lowPrice)
        contentValues.put(COLUMN_HIGH_PRICE, location.highPrice)

        val insert = sqLiteDatabase.insert(LOCATION_TABLE, null, contentValues)
        return -1L != insert
    }

    fun updateAmount(card_info: MTGCardInfo, add : Boolean = false) : Boolean {
        val contentValues = ContentValues()
        val foilInt = if (card_info.isFoil) 1 else 0

        if (add) {
            contentValues.put(COLUMN_AMOUNT, ++card_info.amount)
        } else {
            contentValues.put(COLUMN_AMOUNT, --card_info.amount)
        }
        val update = sqLiteDatabase.update(
            COLLECTION_TABLE, contentValues, "$COLUMN_CARD_NAME=? AND $COLUMN_CARD_SET=? AND $COLUMN_IS_FOIL=?",
            arrayOf(card_info.card_name, card_info.set, foilInt.toString()))

        return update == 1
    }

    fun updatePrice(card_info: MTGCardInfo, new_prices: Prices) : Boolean {
        val contentValues = ContentValues()

        contentValues.put(COLUMN_USD, new_prices.usd)
        contentValues.put(COLUMN_USD_FOIL, new_prices.usd_foil)
        contentValues.put(COLUMN_EUR, new_prices.eur)
        contentValues.put(COLUMN_EUR_FOIL, new_prices.eur_foil)
        contentValues.put(COLUMN_TIX, new_prices.tix)
        contentValues.put(COLUMN_TIX_FOIL, new_prices.tix_foil)
        contentValues.put(COLUMN_PRICE_LAST_UPDATED, SimpleDateFormat("yyyy-MM-dd HH:mm:SS.SSS", Locale.US).format(Calendar.getInstance().time))

        val update = sqLiteDatabase.update(
        COLLECTION_TABLE, contentValues, "$COLUMN_ID=? AND $COLUMN_IS_FOIL=?",
            arrayOf(card_info.id, if (card_info.isFoil) "1" else "0"))

        return update == 1
    }

    fun removeOne(card_info: MTGCardInfo) : Boolean {
        val cardInDB = getOneCard(card_info)

        return if (cardInDB != null && cardInDB.amount > 1) {
            updateAmount(card_info, false)
        } else {
            val foilInt = if (card_info.isFoil) 1 else 0
            card_info.amount = 0
            val delete = sqLiteDatabase.delete(
                COLLECTION_TABLE, "$COLUMN_CARD_NAME=? AND $COLUMN_CARD_SET=? AND $COLUMN_IS_FOIL=?",
                arrayOf(card_info.card_name, card_info.set, foilInt.toString()))

            delete == 1
        }
    }

    fun removeLocation(id : Int) : Boolean {
        val delete = sqLiteDatabase.delete(
            LOCATION_TABLE, "$COLUMN_LOCATION_ID=?",
            arrayOf(id.toString())
        )
        if (delete == 1) {
            val contentValues = ContentValues()
            contentValues.putNull(COLUMN_LOCATION_ID)
            sqLiteDatabase.update(
                CARD_IN_LOCATION_TABLE, contentValues, "$COLUMN_LOCATION_ID=?",
                arrayOf(id.toString())
            )
        }
        return delete == 1
    }

    fun getAllCards(ordering: Ordering = Ordering.NONE, daysAgo : Int = 0) : ArrayList<MTGCardInfo> {
        val returnList = ArrayList<MTGCardInfo>()
        val queryString : String = if (daysAgo > 0) {
            val startDate = Calendar.getInstance()
            startDate.add(Calendar.DATE, -daysAgo)
            val isoString = SimpleDateFormat("yyyy-MM-dd HH:mm:SS.SSS", Locale.US).format(startDate.time)
            "SELECT * FROM $COLLECTION_TABLE WHERE $COLUMN_PRICE_LAST_UPDATED < '$isoString'" + getOrderingString(ordering)
        } else {
            "SELECT * FROM $COLLECTION_TABLE" + getOrderingString(ordering)
        }

        val cursor = sqLiteDatabase.rawQuery(queryString, null)

        if (cursor.moveToFirst()) {
            var cardInfo : MTGCardInfo
            do {
                cardInfo = cursorToCardInfo(cursor)!!
                returnList.add(cardInfo)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return returnList
    }

    fun getAllCardsInLocation(locationId: Int, ordering: Ordering = Ordering.NONE) : ArrayList<MTGCardInfo> {
        if (locationId < 0) {
            return getAllCards(ordering)
        }
        val returnList = ArrayList<MTGCardInfo>()

        val queryStringCards = "SELECT ${COLLECTION_TABLE}.$COLUMN_ID, $COLUMN_CARD_NAME, $COLUMN_CARD_SET, ${COLLECTION_TABLE}.$COLUMN_IS_FOIL, $COLUMN_RARITY, ${CARD_IN_LOCATION_TABLE}.$COLUMN_AMOUNT AS $COLUMN_AMOUNT," +
                " $COLUMN_USD, $COLUMN_USD_FOIL, $COLUMN_EUR, $COLUMN_EUR_FOIL, $COLUMN_TIX, $COLUMN_TIX_FOIL, $COLUMN_PRICE_LAST_UPDATED" +
                " FROM $COLLECTION_TABLE INNER JOIN $CARD_IN_LOCATION_TABLE ON ${COLLECTION_TABLE}.$COLUMN_ID = ${CARD_IN_LOCATION_TABLE}.$COLUMN_ID AND ${COLLECTION_TABLE}.$COLUMN_IS_FOIL = ${CARD_IN_LOCATION_TABLE}.$COLUMN_IS_FOIL" +
                " WHERE ${CARD_IN_LOCATION_TABLE}.$COLUMN_LOCATION_ID=$locationId" + getOrderingString(ordering)

        val cursor = sqLiteDatabase.rawQuery(queryStringCards, null)

        if (cursor.moveToFirst()) {
            var cardInfo : MTGCardInfo
            do {
                cardInfo = cursorToCardInfo(cursor)!!
                returnList.add(cardInfo)
            }while (cursor.moveToNext())
        }
        cursor.close()

        return returnList
    }

    private fun getOrderingString(ordering: Ordering) : String {
        val returnString : String = when(ordering) {
            Ordering.NAME_ASC -> " ORDER BY $COLLECTION_TABLE.$COLUMN_CARD_NAME ASC"
            Ordering.NAME_DESC -> " ORDER BY $COLLECTION_TABLE.$COLUMN_CARD_NAME DESC"
            Ordering.PRICE_ASC -> " ORDER BY $COLLECTION_TABLE.$COLUMN_USD ASC"
            Ordering.PRICE_DESC -> " ORDER BY $COLLECTION_TABLE.$COLUMN_USD DESC"
            Ordering.NONE -> ""
        }

        return returnString
    }

    fun getAllBoxes() : ArrayList<LocationInfo> {
        val returnList = ArrayList<LocationInfo>()

        val queryString = "SELECT * FROM $LOCATION_TABLE"

        val cursor = sqLiteDatabase.rawQuery(queryString, null)

        if (cursor.moveToFirst()) {
            var locationInfo : LocationInfo
            do {
                locationInfo = cursorToLocationInfo(cursor)!!
                returnList.add(locationInfo)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return returnList
    }

    private fun getOneCard(card_info: MTGCardInfo) : MTGCardInfo?{
        val foilInt = if (card_info.isFoil) 1 else 0

        val queryString = "SELECT * FROM $COLLECTION_TABLE WHERE $COLUMN_CARD_NAME=? AND $COLUMN_CARD_SET=? AND $COLUMN_IS_FOIL=?"

        val cursor = sqLiteDatabase.rawQuery(queryString, arrayOf(card_info.card_name, card_info.set, foilInt.toString()))

        cursor.moveToFirst()
        val returnCard = cursorToCardInfo(cursor)

        cursor.close()
        return returnCard
    }

    private fun getCardInLocation(cardId : String, isCardFoil : Boolean, locationId: Int?) : CardsInLocationInfo? {
        val foilInt = if (isCardFoil) 1 else 0

        val queryString = "SELECT * FROM $CARD_IN_LOCATION_TABLE WHERE $COLUMN_ID=? AND $COLUMN_IS_FOIL=? AND $COLUMN_LOCATION_ID=?"

        val cursor = sqLiteDatabase.rawQuery(queryString, arrayOf(cardId, foilInt.toString(), locationId.toString()))

        cursor.moveToFirst()
        val returnCardInLocation = cursorToCardsInLocationInfo(cursor)

        cursor.close()
        return returnCardInLocation
    }

    private fun removeCardInLocation(cardId : String, isCardFoil : Boolean, locationId: Int?) : Boolean {
        val delete = sqLiteDatabase.delete(
            CARD_IN_LOCATION_TABLE, "$COLUMN_ID=? AND $COLUMN_IS_FOIL=? AND $COLUMN_LOCATION_ID=?",
            arrayOf(cardId, isCardFoil.toString(), locationId.toString()))
        return delete == 1
    }

    companion object {
        const val COLLECTION_TABLE = "CARD_COLLECTION"
        const val COLUMN_ID = "SCRYFALL_ID"
        const val COLUMN_CARD_NAME = "CARD_NAME"
        const val COLUMN_CARD_SET = "CARD_SET"
        const val COLUMN_IS_FOIL = "IS_FOIL"
        const val COLUMN_RARITY = "RARITY"
        const val COLUMN_AMOUNT = "AMOUNT"
        const val COLUMN_USD = "PRICE_USD"
        const val COLUMN_USD_FOIL = "PRICE_USD_FOIL"
        const val COLUMN_EUR = "PRICE_EUR"
        const val COLUMN_EUR_FOIL = "PRICE_EUR_FOIL"
        const val COLUMN_TIX = "PRICE_TIX"
        const val COLUMN_TIX_FOIL = "PRICE_TIX_FOIL"
        const val COLUMN_PRICE_LAST_UPDATED = "PRICES_LAST_UPDATED"

        const val CARD_IN_LOCATION_TABLE = "CARD_LOCATIONS"
        const val LOCATION_TABLE = "LOCATIONS_TABLE"
        const val COLUMN_LOCATION_ID = "LOCATION_ID"
        const val COLUMN_LOCATION_NAME = "LOCATION"
        const val COLUMN_LOW_PRICE = "LOWEST_PRICE"
        const val COLUMN_HIGH_PRICE = "HIGHEST_PRICE"

        fun cursorToCardInfo(cursor: Cursor) : MTGCardInfo? {
            if (cursor.count == 0) return null

            val cardInfo = MTGCardInfo("", "", "", "", 0, Prices())

            cardInfo.id = cursor.getString(0)
            cardInfo.card_name = cursor.getString(1)
            cardInfo.set = cursor.getString(2)
            cardInfo.isFoil = cursor.getInt(3) == 1
            cardInfo.rarity = cursor.getString(4)
            cardInfo.amount = cursor.getInt(5)
            cardInfo.prices.usd = cursorColumnToString(cursor, 6)
            cardInfo.prices.usd_foil = cursorColumnToString(cursor, 7)
            cardInfo.prices.eur = cursorColumnToString(cursor, 8)
            cardInfo.prices.eur_foil = cursorColumnToString(cursor, 9)
            cardInfo.prices.tix = cursorColumnToString(cursor, 10)
            cardInfo.prices.tix_foil = cursorColumnToString(cursor, 11)

            return cardInfo
        }

        fun cursorToLocationInfo(cursor: Cursor) : LocationInfo? {
            if (cursor.count == 0) return null

            val locationInfo = LocationInfo(0, "", null, null)

            locationInfo.locationId = cursor.getInt(0)
            locationInfo.locationName = cursor.getString(1)
            locationInfo.lowPrice = cursor.getFloat(2)
            locationInfo.highPrice = cursor.getFloat(3)

            return locationInfo
        }

        fun cursorToCardsInLocationInfo(cursor : Cursor) : CardsInLocationInfo? {
            if (cursor.count == 0) return null

            val cardsInLocationInfo = CardsInLocationInfo("0", false, 0, 0)

            cardsInLocationInfo.cardId = cursor.getString(0)
            cardsInLocationInfo.isCardFoil = cursor.getInt(1) == 1
            cardsInLocationInfo.locationId = cursor.getInt(2)
            cardsInLocationInfo.amount = cursor.getInt(3)

            return cardsInLocationInfo
        }

        fun cardGroup(cardPrice: String): Pair<Int, String> {
            if (cardPrice == "N/A") return (0 to "N/A")
            val floatPrice = cardPrice.toFloat()
            return when {
                floatPrice >= 10 -> {
                    (4 to "Above 10$")
                }
                floatPrice >= 5 -> {
                    (3 to "Between 5$ - 10$")
                }
                floatPrice >= 1 -> {
                    (2 to "Between 1$ - 5$")
                }
                else -> {
                    (1 to "Bulk")
                }
            }
        }

        private fun cursorColumnToString(cursor: Cursor, column : Int) : String {
            return if (cursor.isNull(column)) {
                "N/A"
            } else {
                String.format("%.2f", cursor.getFloat(column))
            }
        }
    }
}