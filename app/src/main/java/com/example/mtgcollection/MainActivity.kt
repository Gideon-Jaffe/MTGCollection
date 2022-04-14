package com.example.mtgcollection

import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mtgcollection.databinding.ActivityMainBinding
import com.example.mtgcollection.ui.gallery.CollectionDBHelper
import com.example.mtgcollection.ui.slideshow.NotificationDBHelper
import com.example.mtgcollection.ui.slideshow.Notifications
import org.json.JSONObject
import java.text.FieldPosition
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_card_search, R.id.nav_gallery, R.id.nav_notifications, R.id.nav_boxes
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        queue = Volley.newRequestQueue(this)
        updatePrices()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun updatePrices() {
        val collectionTableHelper = CollectionDBHelper(this)
        val cards = collectionTableHelper.getAllCards()
        collectionTableHelper.close()
        for (card in cards) {
            val url = "https://api.scryfall.com//cards//named?exact=${card.card_name}&set=${card.set}"
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response -> updateCardPrice(card, response) },
                null)
            queue.add(jsonObjectRequest)
        }
    }

    private fun updateCardPrice(card:MTGCardInfo, response:JSONObject) {
        val collectionTableHelper = CollectionDBHelper(this)
        val prices = Prices(response.getJSONObject("prices"))
        val (oldPrice, newPrice) = if (card.isFoil) {
            (card.prices.usd_foil!! to if (prices.usd_foil.isNullOrBlank()) "N/A" else (prices.usd_foil!!))
        } else {
            (card.prices.usd!! to if (prices.usd.isNullOrBlank()) "N/A" else (prices.usd!!))
        }
        if (CollectionDBHelper.cardGroup(oldPrice).first != CollectionDBHelper.cardGroup(newPrice).first) {
            createNotification("${card.card_name} Moved to Category ${CollectionDBHelper.cardGroup(newPrice).second} from ${CollectionDBHelper.cardGroup(oldPrice).second}")
        }
        collectionTableHelper.updatePrice(card, prices)
        collectionTableHelper.close()
    }

    private fun createNotification(message: String) {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy")
        val formattedDate = StringBuffer()
        simpleDateFormat.format(java.util.Calendar.getInstance().time, formattedDate, FieldPosition(0))
        val notification = Notifications(0, formattedDate.toString(), "Card Changed Category", message)
        val notificationDBHelper = NotificationDBHelper(this)
        notificationDBHelper.addOne(notification)
        notificationDBHelper.close()
    }
}