package com.example.mtgcollection.ui.slideshow

import java.util.*

data class Notifications(var id:Int, var date: String, var title: String, var text: String, var unread: Boolean = true) {
    fun addText (addedText: String) {
        text += "\n" + addedText
    }
}
