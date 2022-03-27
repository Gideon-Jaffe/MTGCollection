package com.example.mtgcollection.ui.card_search

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mtgcollection.ui.gallery.CollectionDBHelper
import com.example.mtgcollection.databinding.FragmentCardSearchBinding
import com.example.mtgcollection.ChosenCardRecyclerViewAdapter
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mtgcollection.MTGCardInfo
import com.example.mtgcollection.Prices
import android.R
import android.app.Dialog
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import org.json.JSONObject

class CardSearchFragment : Fragment() {

    private lateinit var cardSearchViewModel: CardSearchViewModel
    private var _binding: FragmentCardSearchBinding? = null

    private lateinit var cardAdapter : ChosenCardRecyclerViewAdapter

    private lateinit var queue: RequestQueue

    private lateinit var collectionTableHelper : CollectionDBHelper

    private var width = 0.0F
    private var height = 0.0F

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cardSearchViewModel =
            ViewModelProvider(this)[CardSearchViewModel::class.java]

        _binding = FragmentCardSearchBinding.inflate(inflater, container, false)

        queue = Volley.newRequestQueue(context)

        collectionTableHelper = CollectionDBHelper(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.RecyclerViewChosen.setHasFixedSize(true)
        binding.RecyclerViewChosen.layoutManager = LinearLayoutManager(context)
        binding.RecyclerViewChosen.itemAnimator = DefaultItemAnimator()
        cardAdapter = ChosenCardRecyclerViewAdapter(this.context, ArrayList()) { card, _ -> onListItemClick(card) }
        binding.RecyclerViewChosen.adapter = cardAdapter

        binding.SearchButton.setOnClickListener{onAddButtonClickedAll()}

        val displayMetrics = DisplayMetrics()
        val windowsManager = context?.getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)
        this.width = displayMetrics.widthPixels.toFloat()
        this.height = displayMetrics.heightPixels.toFloat()
    }

    private fun onAddButtonClickedAll() {
        val url = "https://api.scryfall.com//cards//search?q=${binding.cardNameTextBox.text}&unique=prints"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response -> getJsonCardResponseAll(response) },
            {getAutocomplete() })
        queue.add(jsonObjectRequest)
    }

    private fun getJsonCardResponseAll(response: JSONObject) {
        val cardArrayList : ArrayList<MTGCardInfo> = ArrayList()
        val cards = response.getJSONArray("data")
        for (i in 0 until cards.length())
        {
            if (cards.getJSONObject(i).getString("name").equals(binding.cardNameTextBox.text.toString(), true)) {
                val current = cards.getJSONObject(i)
                cardArrayList.add(
                    MTGCardInfo(
                        current.getString("name"),
                        current.getString("set"),
                        current.getString("rarity"),
                        1, Prices(current.getJSONObject("prices"))
                    )
                )
            }
        }
        if (cardArrayList.size != 0) {
            cardAdapter.emptyAdapter()
            cardAdapter.addArray(cardArrayList)
        } else {
            getAutocomplete()
        }

    }

    private fun getAutocomplete () {
        val autocompleteUrl = "https://api.scryfall.com/cards/autocomplete?q=${binding.cardNameTextBox.text}"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, autocompleteUrl, null,
            {response ->
                val arrayNames : ArrayList<String> = ArrayList()
                val names = response.getJSONArray("data")
                for (i in 0 until names.length())
                {
                    arrayNames.add(names.getString(i))
                }
                binding.cardNameTextBox.setAdapter(ArrayAdapter(requireContext(), R.layout.simple_list_item_1, arrayNames))
                binding.cardNameTextBox.showDropDown()
            },
            {error -> Toast.makeText(context, "Auto Complete Error: $error", Toast.LENGTH_SHORT).show()})
        queue.add(jsonObjectRequest)
    }

    private fun onListItemClick(cardInfo: MTGCardInfo) {
        createCardDialog(cardInfo)
    }

    private fun createCardDialog(cardInfo: MTGCardInfo) {
        val dialog = Dialog(this.requireContext())
        dialog.setContentView(com.example.mtgcollection.R.layout.add_card_popup)
        dialog.findViewById<TextView>(com.example.mtgcollection.R.id.popup_card_name).text = cardInfo.card_name
        dialog.findViewById<ImageView>(com.example.mtgcollection.R.id.popup_set_image).setImageDrawable(cardInfo.getSetImage(context))
        dialog.findViewById<EditText>(com.example.mtgcollection.R.id.popup_amount_input).setText("1")

        //set On Click Listener
        dialog.findViewById<Button>(com.example.mtgcollection.R.id.popup_add_button).setOnClickListener {
            cardInfo.isFoil = dialog.findViewById<SwitchCompat>(com.example.mtgcollection.R.id.popup_foil_spinner).isChecked
            val amountText = dialog.findViewById<EditText>(com.example.mtgcollection.R.id.popup_amount_input).text.toString()
            if (amountText != "" && amountText != "0") {cardInfo.amount = amountText.toInt(); dialog.hide(); addCardToCollection(cardInfo)}
            else {Toast.makeText(context, "Need an amount", Toast.LENGTH_SHORT).show()}}
        dialog.window?.setLayout(((width/9)*8).toInt(), ((width/9)*8).toInt())
        dialog.show()
    }

    private fun addCardToCollection(cardInfo: MTGCardInfo) {
        if (collectionTableHelper.addOne(cardInfo)){
            Toast.makeText(context, "Successfully added ${cardInfo.card_name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error Adding ${cardInfo.card_name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        collectionTableHelper.close()
        super.onDestroyView()
        _binding = null
    }
}