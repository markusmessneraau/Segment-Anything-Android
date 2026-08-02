package com.example.sam.ui.screens.boulderDetail

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sam.R

import androidx.compose.ui.platform.LocalContext
import com.example.sam.util.cropToSquareCenter
import com.example.sam.util.decodeSampledBitmapFromResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoulderDetailScreen(
    boulderId: String,
    navController: NavController,
    viewModel: BoulderDetailViewModel
) {
    val boulder by viewModel.boulder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(boulderId) {
        viewModel.fetchBoulder(boulderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(boulder?.name ?: "Boulder Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    )
                )
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF374151)
                    )
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                boulder != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = boulder!!.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Erstellt am: ${boulder!!.createdAt.take(10)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF374151), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = boulder!!.grade,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .onSizeChanged { size = it },
                            contentAlignment = Alignment.TopStart
                        ) {
                            val context = LocalContext.current
                            val backgroundBitmap = remember {
                                val raw = decodeSampledBitmapFromResource(context.resources, R.drawable.spraywall_bg)
                                cropToSquareCenter(raw).asImageBitmap()
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = backgroundBitmap,
                                    contentDescription = "Spraywall Hintergrund",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                )

                                if (size.width > 0 && size.height > 0) {
                                    val density = LocalDensity.current
                                    val ratioX = size.width.toFloat() / 1024f
                                    val ratioY = size.height.toFloat() / 1024f

                                    boulder!!.holds.forEach { hold ->
                                        val decodedBitmap = remember(hold.imageBlob) {
                                            val imageBytes = Base64.decode(hold.imageBlob, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
                                        }

                                        Image(
                                            bitmap = decodedBitmap,
                                            contentDescription = "Hold Mask",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .offset {
                                                    IntOffset(
                                                        x = (hold.xOffset * ratioX).toInt(),
                                                        y = (hold.yOffset * ratioY).toInt()
                                                    )
                                                }
                                                .size(
                                                    width = with(density) { (decodedBitmap.width * ratioX).toDp() },
                                                    height = with(density) { (decodedBitmap.height * ratioY).toDp() }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
