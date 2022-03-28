package com.example.mtgcollection

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.json.JSONObject
import java.lang.Exception

data class MTGCardInfo(var id : String, var card_name : String, var set : String, var rarity : String, var amount : Int, var prices : Prices, var isFoil : Boolean = false) {

    fun getPriceString() : String {
        return "USD: ${prices.usd}\nEUR: ${prices.eur}\nTIX: ${prices.tix}"
    }

    fun getSetImage(context: Context?) : Drawable? {
        return getSetImagePrivate(context, this.set, this.rarity)
    }

    companion object {
        private const val TAG = "MTGCardInfo"

        fun getSetImagePrivate(context : Context?, set :String, rarity : String) : Drawable? {
            val drawable : Drawable? = try {
                ResourcesCompat.getDrawable(
                    context?.resources!!,
                    context.resources.getIdentifier(createIdentifier(set, rarity),
                        "drawable",
                        context.packageName),
                    null
                )
            } catch (E : Exception) {
                null
            }
            return drawable
        }

        private fun createIdentifier(set :String, rarity : String) : String {
            var identifier = String()
            identifier += if (set[0] in '0'..'9' && set.endsWith("ed", true)) {
                "ed${set[0]}"
            } else if (set[0] in '0'..'9' && set[1] in '0'..'9' && set[2] == 'e') {
                "e${set[0]}${set[1]}"
            } else {
                set.lowercase()
            }
            identifier += "_" + rarity.lowercase()
            return identifier
        }
    }
}

data class Prices(var usd : String? = null, var usd_foil : String? = null, var eur : String? = null, var eur_foil : String? = null, var tix : String? = null, var tix_foil : String? = null) {

    constructor(jsonObject: JSONObject) : this(null, null, null, null, null, null) {
        if (jsonObject.has("usd")) {
            usd = if (jsonObject.getString("usd") != "null") {
                jsonObject.getString("usd")
            } else {
                null
            }
        }
        if (jsonObject.has("usd_foil")) {
            usd_foil = if (jsonObject.getString("usd_foil") != "null") {
                jsonObject.getString("usd_foil")
            } else {
                null
            }
        }
        if (jsonObject.has("eur")) {
            eur = if (jsonObject.getString("eur") != "null") {
                jsonObject.getString("eur")
            } else {
                null
            }
        }
        if (jsonObject.has("eur_foil")) {
            eur_foil = if (jsonObject.getString("eur_foil") != "null") {
                jsonObject.getString("eur_foil")
            } else {
                null
            }
        }
        if (jsonObject.has("tix")) {
            tix = if (jsonObject.getString("tix") != "null") {
                jsonObject.getString("tix")
            } else {
                null
            }
        }
        if (jsonObject.has("tix_foil")) {
            tix_foil = if (jsonObject.getString("tix_foil") != "null") {
                jsonObject.getString("tix_foil")
            } else {
                null
            }
        }
    }
}

