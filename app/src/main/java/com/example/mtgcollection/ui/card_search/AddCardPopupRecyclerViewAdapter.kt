package com.example.mtgcollection.ui.card_search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.CardsInLocationInfo
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.R

class AddCardPopupRecyclerViewAdapter(var cardsLocation : ArrayList<CardsInLocationInfo>, var locations : ArrayList<LocationInfo>) : RecyclerView.Adapter<AddCardPopupRecyclerViewAdapter.AddCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddCardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.add_card_popup_list_item,
            parent, false)
        return AddCardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AddCardViewHolder, position: Int) {
        val currentItem = cardsLocation[position]
        val theLocation = locations.find { info -> info.locationId == currentItem.locationId }
        holder.location.text = theLocation?.locationName ?: currentItem.locationId.toString()
        if (currentItem.isCardFoil)
        {
            holder.isFoil.visibility = View.VISIBLE
        } else
        {
            holder.isFoil.visibility = View.INVISIBLE
        }
        holder.amount.text = currentItem.amount.toString()
    }

    override fun getItemCount(): Int {
        return cardsLocation.size
    }

    private fun addItem(singleLocation : CardsInLocationInfo) {
        cardsLocation.add(singleLocation)
        notifyItemInserted(cardsLocation.indexOf(singleLocation))
    }

    class AddCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val location : TextView = itemView.findViewById(R.id.popup_list_location)
        val isFoil : AppCompatImageView = itemView.findViewById(R.id.popup_list_foil)
        val amount : TextView = itemView.findViewById(R.id.popup_list_amount)

        init {}

    }
}