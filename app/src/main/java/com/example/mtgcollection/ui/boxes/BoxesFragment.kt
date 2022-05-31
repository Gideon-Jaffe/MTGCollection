package com.example.mtgcollection.ui.boxes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.R
import com.example.mtgcollection.databinding.FragmentBoxesBinding
import com.example.mtgcollection.ui.gallery.CollectionDBHelper

class BoxesFragment : Fragment() {

    private lateinit var boxesViewModel: BoxesViewModel
    private var _binding: FragmentBoxesBinding? = null

    private lateinit var boxesRecyclerView : RecyclerView

    private lateinit var boxesDBHelper: CollectionDBHelper

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        boxesViewModel =
            ViewModelProvider(this)[BoxesViewModel::class.java]

        _binding = FragmentBoxesBinding.inflate(inflater, container, false)
        val root : View = binding.root

        boxesDBHelper = CollectionDBHelper(requireContext())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boxesRecyclerView = view.findViewById(R.id.Boxes_Locations_RecyclerView)
        boxesRecyclerView.setHasFixedSize(true)
        boxesRecyclerView.layoutManager = LinearLayoutManager(context)
        boxesRecyclerView.itemAnimator = DefaultItemAnimator()
        getBoxesData()

        binding.BoxesAddButton.setOnClickListener{onAddLocationButtonClicked()}
    }

    private fun getBoxesData() {
        val items = boxesDBHelper.getAllBoxes()
        boxesRecyclerView.adapter = LocationsRecyclerViewAdapter(this.context, items, {id -> boxesDBHelper.removeLocation(id)}){ location, pos -> onBoxesListItemClick(location, pos)}
    }

    private fun onBoxesListItemClick(location: LocationInfo, position : Int) {
        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
    }

    private fun onAddLocationButtonClicked() {
        createLocationDialog()
    }

    private fun createLocationDialog() {
        val dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.add_location_popup)

        //set On Click Listener
        dialog.findViewById<Button>(R.id.location_popup_add_button).setOnClickListener {
            val name = dialog.findViewById<EditText>(R.id.location_popup_name_input).text.toString()
            val lowPrice = dialog.findViewById<EditText>(R.id.location_popup_low_price).text.toString().toFloat()
            val highPrice = dialog.findViewById<EditText>(R.id.location_popup_high_price).text.toString().toFloat()
            val location = LocationInfo(0, name, lowPrice, highPrice)
            dialog.hide(); addLocationToCollection(location)}
        dialog.show()
    }

    private fun addLocationToCollection(location : LocationInfo) {
        if (boxesDBHelper.addLocation(location)){
            Toast.makeText(context, "Successfully added ${location.locationName} location", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error Adding ${location.locationName} location", Toast.LENGTH_SHORT).show()
        }
    }
}