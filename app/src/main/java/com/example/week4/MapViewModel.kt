package com.example.week4

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.osmdroid.util.GeoPoint

class MapViewModel(app:Application): AndroidViewModel(app){
    var openTopoMap by mutableStateOf(false)
    var recentLong by mutableStateOf("")
    var recentLat by mutableStateOf("")
    var currentLoc by mutableStateOf(GeoPoint(51.05, -0.72))
}
