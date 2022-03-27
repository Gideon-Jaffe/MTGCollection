package com.example.mtgcollection.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.R
import com.example.mtgcollection.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    private lateinit var newRecyclerview : RecyclerView
    private lateinit var newArrayList : ArrayList<MTGCardInfo>

    private lateinit var collectionDBHelper: CollectionDBHelper
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this)[GalleryViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        newArrayList = ArrayList()

        collectionDBHelper = CollectionDBHelper(requireContext())
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newRecyclerview = view.findViewById(R.id.recView)
        newRecyclerview.setHasFixedSize(true)
        newRecyclerview.layoutManager = LinearLayoutManager(context)
        newRecyclerview.itemAnimator = DefaultItemAnimator()
        getUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserData() {
        newRecyclerview.adapter = CollectionRecyclerViewAdapter(this.context, collectionDBHelper.getAll()) { card, pos -> onCollectionListItemClick(card, pos)}
    }

    private fun onCollectionListItemClick(cardInfo: MTGCardInfo, position : Int) {
        if (cardInfo.amount > 1) {
            if (collectionDBHelper.updateAmount(cardInfo)) {
                if (cardInfo.amount > 0) {
                    newRecyclerview.adapter?.notifyItemChanged(position)
                }
            } else {
                Toast.makeText(context, "Failure Removing ${cardInfo.card_name}", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (collectionDBHelper.removeOne(cardInfo)) {
                newRecyclerview.adapter?.notifyItemRemoved(position)
            } else {
                Toast.makeText(context, "Failure Removing ${cardInfo.card_name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}