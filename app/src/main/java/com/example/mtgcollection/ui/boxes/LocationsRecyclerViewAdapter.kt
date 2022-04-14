package com.example.mtgcollection.ui.boxes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.R

class LocationsRecyclerViewAdapter(private val context : Context?, private val locationsInfoList : ArrayList<LocationInfo>, private val onItemClicked: (locationClicked: LocationInfo, position : Int) -> Unit) :
    RecyclerView.Adapter<LocationsRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.location_list_item,
            parent, false)
        return MyViewHolder(itemView) { i -> onItemClicked(locationsInfoList[i], i)}
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = locationsInfoList[position]
        holder.locationName.text = currentItem.locationName
        if (currentItem.lowPrice != null) {
            holder.lowPrice.text = "From ${currentItem.lowPrice.toString()}"
        }
        if (currentItem.highPrice != null) {
            holder.highPrice.text = "To ${currentItem.highPrice.toString()}"
        }
    }

    private fun addItem(location : LocationInfo) {
        locationsInfoList.add(location)
        notifyItemInserted(locationsInfoList.indexOf(location))
    }

    fun emptyAdapter() {
        for (i in locationsInfoList.size - 1 downTo 0) {
            locationsInfoList.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    fun addArray(locations : ArrayList<LocationInfo>) {
        for (i in 0 until locations.size) {
            addItem(locations[i])
        }
    }

    override fun getItemCount(): Int {
        return locationsInfoList.size
    }

    class MyViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val locationName : TextView = itemView.findViewById(R.id.location_name)
        val lowPrice : TextView = itemView.findViewById(R.id.location_low_price)
        val highPrice : TextView = itemView.findViewById(R.id.location_high_price)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }
}