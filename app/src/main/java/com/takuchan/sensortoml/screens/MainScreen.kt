package com.takuchan.sensortoml.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

// センサーから受け取るデータモデル
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

data class SensorSnapshot(
    val acceleration: Vector3 = Vector3(),
    val gravity: Vector3 = Vector3(),
    val gyroscope: Vector3 = Vector3(),
    val magneticField: Vector3 = Vector3(),
    val latitude: Double? = null,
    val longitude: Double? = null
)

private enum class MeasurementState {
    IDLE, WAITING, RECORDING
}

// UI表示用の仮履歴。後からRoomのEntityに置き換える
private data class SessionItem(
    val id: Int,
    val durationSeconds: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sensorSnapshot: SensorSnapshot = SensorSnapshot()
) {
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var bufferSeconds by rememberSaveable { mutableIntStateOf(3) }

    var measurementState by remember {
        mutableStateOf(MeasurementState.IDLE)
    }
    var countdown by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    val history = remember { mutableStateListOf<SessionItem>() }

    // 開始までの待機カウントダウン
    LaunchedEffect(measurementState, bufferSeconds) {
        if (measurementState == MeasurementState.WAITING) {
            countdown = bufferSeconds

            while (
                countdown > 0 &&
                measurementState == MeasurementState.WAITING
            ) {
                delay(1000)
                countdown--
            }

            if (measurementState == MeasurementState.WAITING) {
                elapsedSeconds = 0
                measurementState = MeasurementState.RECORDING
            }
        }
    }

    // 計測経過時間
    LaunchedEffect(measurementState) {
        if (measurementState == MeasurementState.RECORDING) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        if (showHistory) "計測履歴"
                        else "Collect Geo Sensor"
                    )
                },
                navigationIcon = {
                    if (showHistory) {
                        IconButton(
                            onClick = { showHistory = false }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "戻る"
                            )
                        }
                    }
                },
                actions = {
                    if (!showHistory) {
                        IconButton(
                            onClick = { showHistory = true }
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "計測履歴"
                            )
                        }

                        IconButton(
                            onClick = { showSettings = true }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "設定"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!showHistory) {
                Surface(shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                when (measurementState) {
                                    MeasurementState.IDLE -> {
                                        measurementState =
                                            MeasurementState.WAITING
                                    }

                                    MeasurementState.WAITING -> {
                                        measurementState =
                                            MeasurementState.IDLE
                                    }

                                    MeasurementState.RECORDING -> {
                                        // 仮履歴。Room保存は後で実装
                                        history.add(
                                            0,
                                            SessionItem(
                                                id = history.size + 1,
                                                durationSeconds = elapsedSeconds
                                            )
                                        )
                                        measurementState =
                                            MeasurementState.IDLE
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (measurementState ==
                                        MeasurementState.IDLE
                                    ) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                            )
                        ) {
                            Text(
                                when (measurementState) {
                                    MeasurementState.IDLE ->
                                        "計測を開始"
                                    MeasurementState.WAITING ->
                                        "開始をキャンセル"
                                    MeasurementState.RECORDING ->
                                        "計測を停止"
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        if (showHistory) {
            HistoryContent(
                history = history,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MeasurementStatusCard(
                        state = measurementState,
                        countdown = countdown,
                        elapsedSeconds = elapsedSeconds
                    )
                }

                item {
                    Text(
                        "リアルタイムセンサーデータ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    SensorCard(
                        title = "加速度",
                        unit = "m/s²",
                        vector = sensorSnapshot.acceleration
                    )
                }

                item {
                    SensorCard(
                        title = "重力",
                        unit = "m/s²",
                        vector = sensorSnapshot.gravity
                    )
                }

                item {
                    SensorCard(
                        title = "ジャイロスコープ",
                        unit = "rad/s",
                        vector = sensorSnapshot.gyroscope
                    )
                }

                item {
                    SensorCard(
                        title = "地磁気",
                        unit = "µT",
                        vector = sensorSnapshot.magneticField
                    )
                }

                item {
                    LocationCard(
                        latitude = sensorSnapshot.latitude,
                        longitude = sensorSnapshot.longitude
                    )
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            bufferSeconds = bufferSeconds,
            onDismiss = { showSettings = false },
            onSave = {
                bufferSeconds = it
                showSettings = false
            }
        )
    }
}

@Composable
private fun MeasurementStatusCard(
    state: MeasurementState,
    countdown: Int,
    elapsedSeconds: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                when (state) {
                    MeasurementState.IDLE -> "待機中"
                    MeasurementState.WAITING -> "計測開始まで"
                    MeasurementState.RECORDING -> "計測中"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                when (state) {
                    MeasurementState.IDLE -> "00:00"
                    MeasurementState.WAITING -> "${countdown} 秒"
                    MeasurementState.RECORDING ->
                        formatDuration(elapsedSeconds)
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                when (state) {
                    MeasurementState.IDLE ->
                        "開始ボタンを押すと計測を開始します"
                    MeasurementState.WAITING ->
                        "設定した待機時間をカウント中"
                    MeasurementState.RECORDING ->
                        "センサーデータを収集中"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SensorCard(
    title: String,
    unit: String,
    vector: Vector3
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AxisValue("X", vector.x)
                AxisValue("Y", vector.y)
                AxisValue("Z", vector.z)
            }
        }
    }
}

@Composable
private fun RowScope.AxisValue(
    axis: String,
    value: Float
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            axis,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(4.dp))

        Text(
            String.format(Locale.US, "%.3f", value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LocationCard(
    latitude: Double?,
    longitude: Double?
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "位置情報（GPS）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            Text(
                "緯度: " + (
                        latitude?.let {
                            String.format(Locale.US, "%.6f", it)
                        } ?: "---"
                        )
            )

            Text(
                "経度: " + (
                        longitude?.let {
                            String.format(Locale.US, "%.6f", it)
                        } ?: "---"
                        )
            )
        }
    }
}

@Composable
private fun HistoryContent(
    history: List<SessionItem>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("計測履歴はありません")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history, key = { it.id }) { session ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "計測 #${session.id}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "計測時間: " +
                                    formatDuration(session.durationSeconds)
                        )
                        Text(
                            "CSV / TXT出力は今後実装",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    bufferSeconds: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var input by remember(bufferSeconds) {
        mutableStateOf(bufferSeconds.toString())
    }

    val parsed = input.toIntOrNull()
    val valid = parsed != null && parsed in 0..60

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("計測設定") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("開始ボタンを押してから計測するまでの待機時間")

                OutlinedTextField(
                    value = input,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            input = newValue
                        }
                    },
                    label = { Text("待機時間（秒）") },
                    supportingText = { Text("0〜60秒で設定") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(parsed!!) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remaining = seconds % 60

    return if (hours > 0) {
        String.format(
            Locale.US, "%02d:%02d:%02d",
            hours, minutes, remaining
        )
    } else {
        String.format(
            Locale.US, "%02d:%02d",
            minutes, remaining
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    MaterialTheme {
        MainScreen(
            sensorSnapshot = SensorSnapshot(
                acceleration = Vector3(0.12f, 1.84f, 9.61f),
                gravity = Vector3(0.03f, 0.12f, 9.80f),
                gyroscope = Vector3(0.02f, -0.15f, 0.08f),
                magneticField = Vector3(23.4f, -8.1f, 41.7f),
                latitude = 35.681236,
                longitude = 139.767125
            )
        )
    }
}
