package com.example.mtgcollection.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.R
import com.example.mtgcollection.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    private lateinit var locationSpinner : Spinner
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
    ): View {
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

        locationSpinner = view.findViewById(R.id.gallery_location_spinner)
        setLocations()

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

    private fun setLocations() {
        val locationsArray = ArrayList<LocationInfo>()
        locationsArray.add(LocationInfo(-1, "ALL", 0.0F, 10000.0F))
        locationsArray.addAll(collectionDBHelper.getAllBoxes())

        locationSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, locationsArray)
        locationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val loc : LocationInfo = locationSpinner.selectedItem as LocationInfo
                getCardsInBox(loc.locationId!!)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                getUserData()
            }

        }
    }

    private fun getUserData() {
        newRecyclerview.adapter = CollectionRecyclerViewAdapter(this.context, collectionDBHelper.getAllCards()) { card, pos -> onCollectionListItemClick(card, pos)}
    }

    private fun getCardsInBox(boxId : Int) {
        newRecyclerview.adapter = CollectionRecyclerViewAdapter(this.context, collectionDBHelper.getAllCardsInLocation(boxId)) { card, pos -> onCollectionListItemClick(card, pos)}
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