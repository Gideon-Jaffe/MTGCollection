package com.example.mtgcollection.ui.gallery

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
import com.example.mtgcollection.CardsInLocationInfo
import com.example.mtgcollection.LocationInfo
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.R
import com.example.mtgcollection.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    private lateinit var locationSpinner : Spinner
    private lateinit var orderMethodSpinner : Spinner
    private lateinit var orderAscDesSpinner : Spinner
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

        newRecyclerview = view.findViewById(R.id.recView)
        newRecyclerview.setHasFixedSize(true)
        newRecyclerview.layoutManager = LinearLayoutManager(context)
        newRecyclerview.itemAnimator = DefaultItemAnimator()

        orderMethodSpinner = view.findViewById(R.id.gallery_order_by_spinner)
        orderAscDesSpinner = view.findViewById(R.id.gallery_des_asc_spinner)
        setOrderMethod()

        locationSpinner = view.findViewById(R.id.gallery_location_spinner)
        setLocations()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setOrderMethod() {
        val orderTypes = ArrayList<String>(listOf("Name", "Price"))
        val ascDes = ArrayList<String>(listOf("Ascending", "Descending"))

        orderMethodSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, orderTypes)
        orderAscDesSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ascDes)
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
        newRecyclerview.adapter = CollectionRecyclerViewAdapter(this.context, collectionDBHelper.getAllCards(getOrderingMethod())) { card, pos -> onCollectionListItemClick(card, pos)}
    }

    private fun getCardsInBox(boxId : Int) {
        val order = getOrderingMethod()
        val cards = collectionDBHelper.getAllCardsInLocation(boxId, order)
        newRecyclerview.adapter = CollectionRecyclerViewAdapter(this.context, cards) { card, pos -> onCollectionListItemClick(card, pos)}
    }

    private fun getOrderingMethod() : CollectionDBHelper.Ordering {
        val method = orderMethodSpinner.selectedItem as String
        val direction = orderAscDesSpinner.selectedItem as String

        return when(method + direction) {
            "NameAscending" -> CollectionDBHelper.Ordering.NAME_ASC
            "NameDescending" -> CollectionDBHelper.Ordering.NAME_DESC
            "PriceAscending" -> CollectionDBHelper.Ordering.PRICE_ASC
            "PriceDescending" -> CollectionDBHelper.Ordering.PRICE_DESC
            else -> CollectionDBHelper.Ordering.NONE
        }
    }

    private fun onCollectionListItemClick(cardInfo: MTGCardInfo, position : Int) {
        if ((locationSpinner.selectedItem as LocationInfo).locationId == -1) chooseLocationAndRemove(cardInfo)
        else if (collectionDBHelper.removeOne(cardInfo, (locationSpinner.selectedItem as LocationInfo).locationId)) {
            newRecyclerview.adapter?.notifyItemRemoved(position)
        } else {
            Toast.makeText(
                context,
                "Failure Removing ${cardInfo.card_name}",
                Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun chooseLocationAndRemove(cardInfo: MTGCardInfo){
        val cardLocations = collectionDBHelper.getAllLocationsOfCard(cardInfo.id)
        if (cardLocations.size == 1) removeCard(cardLocations[0].cardId, cardLocations[0].isCardFoil, cardLocations[0].locationId)
        else {
            val dialog = Dialog(this.requireContext())
            dialog.setContentView(R.layout.remove_card_popup)
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.remove_card_popup_recyclerview)
            recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
            recyclerView.adapter = RemoveCardPopupRecyclerAdapter(cardLocations, collectionDBHelper.getAllBoxes()) {cardId, isCardFoil, locationId -> dialog.hide(); removeCard(cardId, isCardFoil, locationId)}
            dialog.window?.setLayout(800, 1000)
            dialog.show()
        }
    }

    private fun removeCard(cardId: String, isCardFoil : Boolean, locationId : Int?) {
        if (collectionDBHelper.removeOne(cardId, isCardFoil, locationId)) {
            //TODO(update recycler view)
        } else {
            Toast.makeText(
                context,
                "Failure Removing $cardId",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    class RemoveCardPopupRecyclerAdapter(private val cardsInLocations : ArrayList<CardsInLocationInfo>, private val locations : ArrayList<LocationInfo>, val onItemClick : (String, Boolean, Int?) -> Unit) :
            RecyclerView.Adapter<RemoveCardPopupRecyclerAdapter.ThisViewHolder>() {

                class ThisViewHolder(view : View, private val onItemClick : (Int) -> Unit) : RecyclerView.ViewHolder(view), View.OnClickListener {
                    val thisButton : Button

                    init {
                        thisButton = view.findViewById(R.id.remove_card_popup_list_item_button)
                        thisButton.setOnClickListener(this)
                    }

                    override fun onClick(p0: View?) {
                        onItemClick(adapterPosition)
                    }
                }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThisViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.remove_card_popup_list_item, parent, false)

            return ThisViewHolder(view) { i ->
                onItemClick(
                    cardsInLocations[i].cardId,
                    cardsInLocations[i].isCardFoil,
                    cardsInLocations[i].locationId
                )
            }
        }

        override fun onBindViewHolder(holder: ThisViewHolder, position: Int) {
            holder.thisButton.text = locations.find { locationInfo -> locationInfo.locationId == cardsInLocations[position].locationId }?.locationName
        }

        override fun getItemCount(): Int {
            return cardsInLocations.size
        }
    }
}