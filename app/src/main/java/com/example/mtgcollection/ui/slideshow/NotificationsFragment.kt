package com.example.mtgcollection.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.R
import com.example.mtgcollection.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private var _binding: FragmentNotificationsBinding? = null

    private lateinit var notificationsRecyclerView : RecyclerView
    private lateinit var notificationsArray : ArrayList<Notifications>

    private lateinit var notificationsDBHelper: NotificationDBHelper
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this)[NotificationsViewModel::class.java]

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        notificationsArray = ArrayList()

        notificationsDBHelper = NotificationDBHelper(requireContext())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsRecyclerView = view.findViewById(R.id.NotificationRecView)
        notificationsRecyclerView.setHasFixedSize(true)
        notificationsRecyclerView.layoutManager = LinearLayoutManager(context)
        notificationsRecyclerView.itemAnimator = DefaultItemAnimator()
        getNotificationsData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getNotificationsData() {
        val items = notificationsDBHelper.getAll()
        notificationsRecyclerView.adapter = NotificationRecyclerViewAdapter(this.context, items) { notification, pos -> onNotificationListItemClick(notification, pos)}
    }

    private fun onNotificationListItemClick(notification: Notifications, position : Int) {
        if (notificationsDBHelper.markRead(notification)) {
            notification.unread = false
            notificationsRecyclerView.adapter?.notifyItemChanged(position)
        }
    }
}