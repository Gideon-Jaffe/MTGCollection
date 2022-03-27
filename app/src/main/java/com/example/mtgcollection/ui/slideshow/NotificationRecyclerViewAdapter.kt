package com.example.mtgcollection.ui.slideshow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.R

class NotificationRecyclerViewAdapter(private val context : Context?, private val notificationList : ArrayList<Notifications>, private val onItemClicked: (notificationClicked: Notifications, position :Int) -> Unit) :
    RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.notification_list_item,
            parent, false)
        return NotificationsViewHolder(itemView) {i -> onItemClicked(notificationList[i], i)}
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val currentItem = notificationList[position]
        holder.dateText.text = currentItem.date
        holder.titleText.text = currentItem.title
        holder.notificationText.text = currentItem.text
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class NotificationsViewHolder(itemView : View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val dateText : TextView = itemView.findViewById(R.id.notification_date)
        val titleText : TextView = itemView.findViewById(R.id.notificationTitle)
        val notificationText : TextView = itemView.findViewById(R.id.notificationText)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
                val position = adapterPosition
            onItemClicked(position)
        }
    }
}