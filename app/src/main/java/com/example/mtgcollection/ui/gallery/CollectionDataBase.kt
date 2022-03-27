package com.example.mtgcollection.ui.gallery

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.Prices

class CollectionDBHelper (context: Context) : SQLiteOpenHelper(context, "MyCollection", null, 1) {
    private var sqLiteDatabase: SQLiteDatabase = this.writableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableStatement = "CREATE TABLE $COLLECTION_TABLE (" +
                "$COLUMN_CARD_NAME TEXT, " +
                "$COLUMN_CARD_SET TEXT, " +
                "$COLUMN_IS_FOIL INTEGER, " +
                "$COLUMN_RARITY CHAR, " +
                "$COLUMN_AMOUNT CHAR, " +
                "$COLUMN_USD REAL, " +
                "$COLUMN_USD_FOIL REAL, " +
                "$COLUMN_EUR REAL, " +
                "$COLUMN_EUR_FOIL REAL, " +
                "$COLUMN_TIX REAL, " +
                "$COLUMN_TIX_FOIL REAL, " +
                "$COLUMN_PRICE_LAST_UPDATED TEXT, " +
                "PRIMARY KEY ($COLUMN_CARD_NAME, $COLUMN_CARD_SET, $COLUMN_IS_FOIL))"

        db?.execSQL(createTableStatement)

        /*val createLocationTableStatement = "CREATE TABLE $LOCATION_TABLE (" +
                "$COLUMN_CARD_NAME TEXT, " +
                "$COLUMN_CARD_SET TEXT, "*/
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    override fun close() {
        sqLiteDatabase.close()
        super.close()
    }

    fun addOne(card_info : MTGCardInfo) : Boolean
    {
        if (getOne(card_info) != null) {
            return updateAmount(card_info, true)
        } else {
            val contentValues = ContentValues()

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
            contentValues.put(COLUMN_PRICE_LAST_UPDATED, java.util.Calendar.getInstance().time.toString())

            val insert = sqLiteDatabase.insert(COLLECTION_TABLE, null, contentValues)
            return -1L != insert
        }
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
        contentValues.put(COLUMN_PRICE_LAST_UPDATED, java.util.Calendar.getInstance().time.toString())

        val update = sqLiteDatabase.update(
            COLLECTION_TABLE, contentValues, "$COLUMN_CARD_NAME=? AND $COLUMN_CARD_SET=? AND $COLUMN_IS_FOIL=?",
            arrayOf(card_info.card_name, card_info.set, if (card_info.isFoil) "1" else "0"))

        return update == 1
    }

    fun removeOne(card_info: MTGCardInfo) : Boolean {
        val cardInDB = getOne(card_info)

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

    fun getAll() : ArrayList<MTGCardInfo> {
        val returnList = ArrayList<MTGCardInfo>()

        val queryString = "SELECT * FROM $COLLECTION_TABLE"

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

    fun getOne(card_info: MTGCardInfo) : MTGCardInfo?{
        val foilInt = if (card_info.isFoil) 1 else 0

        val queryString = "SELECT * FROM $COLLECTION_TABLE WHERE $COLUMN_CARD_NAME=? AND $COLUMN_CARD_SET=? AND $COLUMN_IS_FOIL=?"

        val cursor = sqLiteDatabase.rawQuery(queryString, arrayOf(card_info.card_name, card_info.set, foilInt.toString()))

        cursor.moveToFirst()
        val returnCard = cursorToCardInfo(cursor)

        cursor.close()
        return returnCard
    }

    companion object {
        const val COLLECTION_TABLE = "CARD_COLLECTION"
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

        const val LOCATION_TABLE = "CARD_LOCATIONS"
        const val COLUMN_LOCATION = "LOCATION"

        fun cursorToCardInfo(cursor: Cursor) : MTGCardInfo? {
            if (cursor.count == 0) return null

            val cardInfo = MTGCardInfo("", "", "", 0, Prices())

            cardInfo.card_name = cursor.getString(0)
            cardInfo.set = cursor.getString(1)
            cardInfo.isFoil = cursor.getInt(2) == 1
            cardInfo.rarity = cursor.getString(3)
            cardInfo.amount = cursor.getInt(4)
            cardInfo.prices.usd = cursorColumnToString(cursor, 5)
            cardInfo.prices.usd_foil = cursorColumnToString(cursor, 6)
            cardInfo.prices.eur = cursorColumnToString(cursor, 7)
            cardInfo.prices.eur_foil = cursorColumnToString(cursor, 8)
            cardInfo.prices.tix = cursorColumnToString(cursor, 9)
            cardInfo.prices.tix_foil = cursorColumnToString(cursor, 10)

            return cardInfo
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