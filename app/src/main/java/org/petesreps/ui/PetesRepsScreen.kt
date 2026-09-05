package org.petesreps.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.petesreps.data.TrainingBackup
import org.petesreps.data.TrainingBackupCodec
import org.petesreps.data.TrainingDatabase
import org.petesreps.engine.WorkoutEngine
import org.petesreps.model.ExercisePrescription
import org.petesreps.model.MeasureUnit
import org.petesreps.model.TrainingSummary
import org.petesreps.model.Workout

@Composable
fun PetesRepsScreen(database: TrainingDatabase, engine: WorkoutEngine) {
    val context = LocalContext.current
    var historyRevision by remember { mutableIntStateOf(0) }
    var workout by remember(historyRevision) { mutableStateOf(engine.generate(database.profile())) }
    var summary by remember(historyRevision) { mutableStateOf(database.summary()) }
    var completionMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<TrainingBackup?>(null) }
    val actuals = remember(workout.dayNumber, historyRevision) { mutableMapOf<String, Int>() }
    var editVersion by remember(workout.dayNumber, historyRevision) { mutableIntStateOf(0) }
    var started by rememberSaveable(workout.dayNumber, historyRevision) { mutableStateOf(false) }
    var showOverview by rememberSaveable(workout.dayNumber, historyRevision) { mutableStateOf(false) }
    var currentIndex by rememberSaveable(workout.dayNumber, historyRevision) { mutableIntStateOf(0) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error("Android could not open the selected file.")
                output.bufferedWriter().use { writer ->
                    writer.write(TrainingBackupCodec.encode(database.backup()))
                }
            }.onSuccess {
                completionMessage = "Training backup exported."
            }.onFailure { error ->
                completionMessage = "Backup export failed: ${error.message ?: "unknown error"}"
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri)
                    ?: error("Android could not open the selected file.")
                val encoded = input.bufferedReader().use { it.readText() }
                TrainingBackupCodec.decode(encoded)
            }.onSuccess { backup ->
                pendingRestore = backup
            }.onFailure { error ->
                completionMessage = "Backup could not be read: ${error.message ?: "unknown error"}"
            }
        }
    }

    fun completeWorkout() {
        val savedDay = workout.dayNumber
        database.completeWorkout(workout, actuals.toMap())
        summary = database.summary()
        workout = engine.generate(database.profile())
        completionMessage = "Session $savedDay saved."
        started = false
        showOverview = false
        currentIndex = 0
    }

    pendingRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore training history?") },
            text = {
                Text(
                    "This will replace the training history currently on this phone with " +
                        "${backup.workouts.size} saved session${if (backup.workouts.size == 1) "" else "s"}. " +
                        "Export the current history first if you may need it later."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        runCatching { database.restoreBackup(backup) }
                            .onSuccess {
                                pendingRestore = null
                                historyRevision += 1
                                completionMessage = "Training history restored."
                            }
                            .onFailure { error ->
                                pendingRestore = null
                                completionMessage = "Restore failed: ${error.message ?: "unknown error"}"
                            }
                    },
                ) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Cancel") }
            },
        )
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Pete's Reps", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Session ${workout.dayNumber} • ${workout.cycleDay} of 6 • ${workout.plannedMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    completionMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }

                if (!started || showOverview) {
                    item {
                        Text(
                            if (started) "Session overview" else "Today's session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(workout.prescriptions, key = { "overview_${it.exercise.id}" }) { prescription ->
                        OverviewCard(prescription)
                    }
                    item {
                        if (started) {
                            Button(
                                onClick = { showOverview = false },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Return to current movement")
                            }
                        } else {
                            Button(
                                onClick = {
                                    started = true
                                    showOverview = false
                                    currentIndex = 0
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Start session")
                            }
                        }
                    }
                } else {
                    val prescription = workout.prescriptions[currentIndex]
                    item {
                        val ignored = editVersion
                        Text(
                            "Movement ${currentIndex + 1} of ${workout.prescriptions.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.height(8.dp))
                        ExerciseCard(
                            prescription = prescription,
                            actual = actuals[prescription.exercise.id] ?: 0,
                            onActualChange = { value ->
                                actuals[prescription.exercise.id] = value.coerceAtLeast(0)
                                editVersion += 1
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentIndex -= 1 },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Previous") }
                            }
                            if (currentIndex < workout.prescriptions.lastIndex) {
                                Button(
                                    onClick = { currentIndex += 1 },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Next") }
                            }
                        }
                        OutlinedButton(
                            onClick = { showOverview = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("View full session")
                        }
                        if (currentIndex == workout.prescriptions.lastIndex) {
                            Button(
                                onClick = ::completeWorkout,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Complete session")
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Summary(summary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            exportLauncher.launch("petes-reps-backup-${System.currentTimeMillis()}.preps")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Export training backup")
                    }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Restore training backup")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(prescription: ExercisePrescription) {
    val unit = if (prescription.exercise.unit == MeasureUnit.REPS) "reps" else "sec"
    val side = if (prescription.exercise.perSide) " / side" else ""
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(prescription.exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${prescription.sets} × ${prescription.targetPerSet} $unit$side • ${prescription.blockMinutes} min")
            if (prescription.exercise.equipment.isNotEmpty()) {
                Text(
                    prescription.exercise.equipment.joinToString(" • ") {
                        it.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    prescription: ExercisePrescription,
    actual: Int,
    onActualChange: (Int) -> Unit,
) {
    var showCues by rememberSaveable(prescription.exercise.id) { mutableStateOf(false) }
    val unit = if (prescription.exercise.unit == MeasureUnit.REPS) "reps" else "sec"
    val side = if (prescription.exercise.perSide) " / side" else ""
    val increment = if (prescription.exercise.unit == MeasureUnit.SECONDS) 5 else 1

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(prescription.exercise.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${prescription.sets} × ${prescription.targetPerSet} $unit$side • ${prescription.blockMinutes}-minute block")
            prescription.challengeNote?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (prescription.exercise.equipment.isNotEmpty()) {
                Text(
                    "Equipment: " + prescription.exercise.equipment.joinToString(", ") {
                        it.name.lowercase().replace('_', ' ')
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onActualChange((actual - increment).coerceAtLeast(0)) }) { Text("−") }
                Text("Actual: $actual $unit", modifier = Modifier.padding(top = 12.dp))
                OutlinedButton(onClick = { onActualChange(actual + increment) }) { Text("+") }
            }
            OutlinedButton(onClick = { onActualChange(prescription.totalTarget) }) {
                Text("Hit target (${prescription.totalTarget})")
            }
            OutlinedButton(onClick = { showCues = !showCues }) {
                Text(if (showCues) "Hide how" else "How")
            }
            if (showCues) {
                prescription.exercise.cues.forEach { cue -> Text("• $cue", style = MaterialTheme.typography.bodyMedium) }
                if (prescription.exercise.demoAsset == null) {
                    Text("Demo media not added yet.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Summary(summary: TrainingSummary) {
    Text("Training history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("${summary.workoutsLogged} sessions logged • next session ${summary.currentDay}")
}
