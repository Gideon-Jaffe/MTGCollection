package com.example.mtgcollection

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView

class ChosenCardRecyclerViewAdapter(private val context: Context?, private val mtgCardInfoList : ArrayList<MTGCardInfo>, private val onItemClicked: (cardClicked: MTGCardInfo, position : Int) -> Unit) :
    RecyclerView.Adapter<ChosenCardRecyclerViewAdapter.ChosenCardViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChosenCardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chosen_card_list_item,
            parent, false)
        return ChosenCardViewHolder(itemView) { i -> itemClicked(i) }
    }

    private fun itemClicked(position : Int){
        onItemClicked(mtgCardInfoList[position], position)
    }

    override fun onBindViewHolder(holder: ChosenCardViewHolder, position: Int) {
        val currentItem = mtgCardInfoList[position]
        holder.cardName.text = currentItem.card_name
        if (currentItem.getSetImage(context) != null) {
            holder.setImage.setImageDrawable(currentItem.getSetImage(context))
        } else {
            holder.setImage.setImageDrawable(null)
        }
        holder.priceText.text = currentItem.getPriceString()

    }

    override fun getItemCount(): Int {
        return mtgCardInfoList.size
    }

    class ChosenCardViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val cardName : TextView = itemView.findViewById(R.id.CardNameText)
        val setImage : AppCompatImageView = itemView.findViewById(R.id.SetImage)
        val priceText : TextView = itemView.findViewById(R.id.PriceText)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            onItemClicked(position)
        }
    }
}