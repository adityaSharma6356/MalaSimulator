package com.example.malasimulator

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import com.example.malasimulator.ui.theme.MalaSimulatorTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val maineViewModel = viewModels<MainViewModel>().value
        setContent {
            MalaSimulatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        var type1 by remember { mutableStateOf(true) }
                        var refresh by remember { mutableStateOf(false) }
                        var changeMode by remember { mutableStateOf(true) }
                        var gap by remember { mutableStateOf(0.dp) }
                        var change by remember { mutableStateOf(0.9f) }
                        var size by remember { mutableStateOf(1.5f) }
                        var opacity by remember { mutableStateOf(0.63f) }
                        val scope = rememberCoroutineScope()
                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Text(text = "        Bead Count: ${maineViewModel.beads}")
//                            TextButton(onClick = { if(maineViewModel.beads<181) maineViewModel.beads++}) {
//                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
//                            }
//                            TextButton(onClick = { if(maineViewModel.beads>1) maineViewModel.beads--}) {
//                                Icon(painter = painterResource(id = R.drawable.subtract), contentDescription = null)
//                            }
                            IconButton(onClick = {
                                scope.launch {
                                    type1 = false
                                    delay(100)
                                    type1 = true
                                    refresh = false
                                }
                            }) {
                                if(refresh){
                                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                                } else {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)

                                }
                            }
                        }
                        Text(text = "        Change Ratio: $change")
                        Slider(
                            valueRange = 0f..2f,
                            steps = 10,
                            value = change, onValueChange = {
                            change = it
                                refresh = true
                        })
                        Text(text = "        Size: $size")
                        Slider(
                            valueRange = 1f..4f,
                            steps = 10,
                            value = size, onValueChange = {
                            size = it
                                refresh = true
                        })
                        Text(text = "        Opacity: $opacity")
                        Slider(
                            valueRange = 0f..1f,
                            steps = 10,
                            value = opacity, onValueChange = {
                            opacity = it
                        })
                        Row(verticalAlignment = Alignment.CenterVertically){
                           Text(text ="         Blur: ")
                           Switch(checked = changeMode, onCheckedChange = {
                               changeMode = it
                           })
                        }
                        Spacer(modifier = Modifier.weight(1f))


                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            BeadCounter(
                                onSwipe = {
                                    maineViewModel.changeCount()
                                },
                                count = maineViewModel.count,
                                isReady = true,
                                isPlaying = changeMode,
                                isAutoCounting = false,
                                onScreenBeads = maineViewModel.beads,
                                type1 = type1,
                                gap = gap,
                                change = change,
                                size = size,
                                op = opacity,
                                mainViewModel = maineViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeadCounter(
    onSwipe: () -> Unit,
    count: Int,
    isReady: Boolean,
    isPlaying: Boolean,
    isAutoCounting: Boolean,
    onScreenBeads: Int,
    height: Dp = 400.dp,
    type1: Boolean,
    gap:Dp = 0.dp,
    change:Float = 1f,
    size:Float = 1.4f,
    op:Float = 0.2f,
    mainViewModel: MainViewModel
) {
    val listState = rememberPagerState(pageCount = {108+onScreenBeads}, initialPage = onScreenBeads/2)
    val state = rememberPagerState(pageCount = {108})
    val beadHeight = height/onScreenBeads

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        val textState =
            rememberPagerState(pageCount = { 108 }, initialPage = onScreenBeads / 2, initialPageOffsetFraction = 0f)
        Box(modifier = Modifier.padding(start = 50.dp).height(50.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VerticalPager(state = textState, pageSize = PageSize.Fill, reverseLayout = true) {
                    Text(text = it.toString(), fontSize = 25.sp, color = MaterialTheme.colors.primary)
                }
                Text(text = "/108", fontSize = 25.sp,color = MaterialTheme.colors.primary)
            }

        }
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            if(type1){
                remember {
                    derivedStateOf {
                        if(state.currentPage!=mainViewModel.count){
                            mainViewModel.count = state.currentPage
                        }
                    }
                }
                VerticalPager(
                    contentPadding = PaddingValues(0.dp),
                    state = listState,
                    reverseLayout = true,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .zIndex(0f)
                        .height(height)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    pageSize = PageSize.Fixed(beadHeight),
                    beyondBoundsPageCount = onScreenBeads*2
                ){
                    val opacity by remember {
                        derivedStateOf {
                            listState.getOffsetFractionForPage(it)
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.rudraksha),
                        contentDescription = "Bead",
                        modifier = Modifier
                            .padding(vertical = gap)
                            .fillMaxWidth()
                            .zIndex(-(opacity.absoluteValue))
                            .height(beadHeight)
                            .graphicsLayer {
                                translationY = (-200.dp.toPx() + beadHeight.toPx() / 2) - this.size.height/5*opacity*opacity.absoluteValue
                                alpha = (2f - opacity.absoluteValue + op) / 2f
                                if (isPlaying && opacity != 0f) {
                                    val blur =
                                        (opacity.absoluteValue * opacity.absoluteValue).coerceAtLeast(
                                            0.001f
                                        )

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        renderEffect = RenderEffect
                                            .createBlurEffect(
                                                blur, blur, Shader.TileMode.DECAL
                                            )
                                            .asComposeRenderEffect()
                                    }
                                }

                                val scale = size - (opacity.absoluteValue * 0.5f * change)
                                scaleX = scale
                                scaleY = scale
                            }

                    )

                }
                VerticalPager(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize(),
                    beyondBoundsPageCount = 2,
                    reverseLayout = true,
                    pageSpacing = 0.dp,
                ) {
                    Spacer(modifier = Modifier.fillMaxSize())
                }
//            Text(text = item.toString())
                LaunchedEffect(Unit) {
                    snapshotFlow {
                        Pair(
                            state.currentPage,
                            state.currentPageOffsetFraction
                        )
                    }.collect { (page, offset) ->
                        listState.scrollToPage(page+onScreenBeads/2, offset)  // <---
                        textState.scrollToPage(page, offset)  // <---
                    }
                }
                LaunchedEffect(Unit){
                    snapshotFlow {
                        Pair(
                            state.isScrollInProgress,
                            state.currentPage
                        )
                    }.collect {
                        if(!it.first) mainViewModel.count = it.second
                    }
                }
            } else {

            }
        }
    }

}


class MainViewModel: ViewModel(){
    var count by mutableStateOf(0)
    var beads by mutableStateOf(5)
    var counting by mutableStateOf(false)
    var drag by mutableStateOf(0)

    var job1: Job? = null
    fun changeCount(){
        if(count>=108){
            count=0
            return
        }
        count++

    }

}



//


















