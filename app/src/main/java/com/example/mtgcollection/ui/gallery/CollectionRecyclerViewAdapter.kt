package com.example.mtgcollection.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.R
import com.google.android.material.imageview.ShapeableImageView

class CollectionRecyclerViewAdapter(private val context : Context?, private val mtgCardInfoList : ArrayList<MTGCardInfo>, private val onItemClicked: (cardClicked: MTGCardInfo, position : Int) -> Unit) :
    RecyclerView.Adapter<CollectionRecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item,
            parent, false)
        return MyViewHolder(itemView) { i -> onItemClicked(mtgCardInfoList[i], i);
            updateView(i) }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = mtgCardInfoList[position]
        holder.amountText.text = currentItem.amount.toString()
        holder.cardName.text = currentItem.card_name
        if (!currentItem.isFoil) {
            holder.priceText.text = currentItem.prices.usd
        } else {
            holder.priceText.text = currentItem.prices.usd_foil
        }
        if (currentItem.getSetImage(context) == null) {
            holder.setText.visibility = VISIBLE
            holder.setText.text = currentItem.set.uppercase()
            holder.setImage.visibility = INVISIBLE
        } else {
            holder.setImage.setImageDrawable(currentItem.getSetImage(context))
        }
    }

    private fun updateView(position: Int) {
        if (mtgCardInfoList[position].amount <= 1) {
            mtgCardInfoList.removeAt(position)
            notifyItemRemoved(position)
        }
        else {
            mtgCardInfoList[position].amount--
            notifyItemChanged(position)
        }
    }

    private fun getRarityColor (rarity : String) : Int {
        //todo "Return color of rarities"
        return if (rarity == "common") {
            100
        } else if (rarity == "uncommon") {
            200
        } else if (rarity == "rare") {
            300
        } else if (rarity == "mythic") {
            400
        } else {
            0
        }
    }

    private fun addItem(card : MTGCardInfo) {
        mtgCardInfoList.add(card)
        notifyItemInserted(mtgCardInfoList.indexOf(card))
    }

    fun emptyAdapter() {
        for (i in mtgCardInfoList.size - 1 downTo 0) {
            mtgCardInfoList.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    fun addArray(cards : ArrayList<MTGCardInfo>) {
        for (i in 0 until cards.size) {
            addItem(cards[i])
        }
    }

    override fun getItemCount(): Int {
        return mtgCardInfoList.size
    }

    class MyViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val amountText : TextView = itemView.findViewById(R.id.amount)
        val cardName : TextView = itemView.findViewById(R.id.cardName)
        val priceText : TextView = itemView.findViewById(R.id.priceText)
        val setImage : ShapeableImageView = itemView.findViewById(R.id.setImageCollection)
        val setText : TextView = itemView.findViewById(R.id.SetTextCollection)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }
}