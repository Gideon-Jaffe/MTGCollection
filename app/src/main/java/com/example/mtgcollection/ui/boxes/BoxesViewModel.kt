package com.example.mtgcollection.ui.boxes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BoxesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is boxes Fragment"
    }
    val text: LiveData<String> = _text
}