package com.example.week4

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.week4.ui.theme.Week4Theme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.reflect.KProperty
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Week4Theme {
                val navController = rememberNavController()
                val viewModel : MapViewModel by viewModels()
                var openTopoMap by remember { mutableStateOf(false) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    NavHost(navController = navController, startDestination = "MapComposable"){
                        composable("MapComposable"){
                            MapComposable(settingsScreenCallBack = {navController.navigate("SettingsScreen")},
                                viewModel, geoPoint = GeoPoint(51.05, -0.72), openTopoMap = openTopoMap )
                        }
                        composable("SettingsScreen"){
                            SettingsScreen(openTopoMapCallBack =  {recentLat, recentLong, openTopo ->
                                viewModel.currentLoc = GeoPoint(recentLat, recentLong)
                                openTopoMap = openTopo
                                navController.navigate("MapComposable")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapComposable(settingsScreenCallBack: () -> Unit, viewModel: MapViewModel, geoPoint: GeoPoint, openTopoMap: Boolean) {

    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .zIndex(2.0f)) {
            Button(onClick = { settingsScreenCallBack() }) {
                Text("Settings")
            }
        }

        AndroidView(
            factory = { ctx ->
                // This line sets the user agent, a requirement to download OSM maps
                Configuration.getInstance()
                    .load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

                MapView(ctx).apply {
                    setClickable(true)
                    setMultiTouchControls(true)
                    setTileSource(if(openTopoMap)TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)
                    controller.setZoom(14.0)
                }
            },

            update = { view ->
                view.controller.setCenter(viewModel.currentLoc)
                view.setTileSource(if(openTopoMap) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK)
            }
        )
    }
}

@Composable
fun SettingsScreen(openTopoMapCallBack: (Double, Double, Boolean) -> Unit){

    var recentLong by remember {mutableStateOf("")}
    var recentLat by remember { mutableStateOf("") }
    var openTopoMap by remember { mutableStateOf(false) }

    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .zIndex(2.0f)) {
            TextField(
                value = recentLong,
                onValueChange = { newLon -> recentLong = newLon },
                modifier = Modifier.weight(1.0f),
                label = { Text("Longitude") })
            TextField(
                value = recentLat,
                onValueChange = { recentLat = it },
                modifier = Modifier.weight(1.0f),
                label = { Text("Latitude") })

            Switch(checked = openTopoMap, onCheckedChange = { openTopoMap = it })
        }

        Row {
            Button(onClick = { openTopoMapCallBack(recentLat.toDouble(), recentLong.toDouble(), openTopoMap)}) {
                Text("Back to Map")
            }
        }
    }
}