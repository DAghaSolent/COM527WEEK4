package com.example.week4

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Week4Theme {
                val navController = rememberNavController()
                val viewModel : MapViewModel by viewModels()
                var openTopoMap by remember { mutableStateOf(false) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                // A surface container using the 'background' color from the theme
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = true,
                                onClick = { navController.navigate("MapComposable") },
                                icon = { Icon(
                                    painter = painterResource(R.drawable.map_asset),
                                    contentDescription = "Map",
                                    tint = MaterialTheme.colorScheme.primary
                                )},
                                label = { Text("Mapping Home Page")}
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate("SettingsScreen") },
                                icon = { Icon(Icons.Filled.Settings, "Settings Screen")},
                                label = { Text("Setting Screen Page")}
                            )
                        }
                    },
                    topBar = {
                        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ), actions = {
                            IconButton(onClick = {
                                coroutineScope.launch{if(drawerState.isClosed){
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                                }
                            }){
                                Icon(imageVector = Icons.Filled.Menu, "Menu")
                            }
                        }, title = {Text("Top Bar Example")})
                    }
                ){
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ModalNavigationDrawer(drawerState = drawerState,
                            drawerContent = {

                                ModalDrawerSheet(modifier = Modifier.height(200.dp)){

                                    NavigationDrawerItem(
                                        selected = false,
                                        label = {Text("Map")},
                                        onClick = {
                                            coroutineScope.launch { drawerState.close() }
                                            navController.navigate("MapComposable")
                                        }
                                    )

                                    NavigationDrawerItem(
                                        selected = false,
                                        label = {Text("Settings")},
                                        onClick = {
                                            coroutineScope.launch {drawerState.close()}
                                            navController.navigate("SettingsScreen")
                                        }
                                    )
                                }
                            }
                        )
                        {
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
    }
}

@Composable
fun MapComposable(settingsScreenCallBack: () -> Unit, viewModel: MapViewModel, geoPoint: GeoPoint, openTopoMap: Boolean) {

    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .zIndex(2.0f)) {
            Button(onClick = { settingsScreenCallBack() }) {
                Icon(Icons.Filled.Settings, "Settings Page")
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
                Icon(Icons.Filled.ArrowBack, "Back To Map Button")
                Text("Back to Map")
            }
        }
    }
}