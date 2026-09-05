package org.petesreps.ui

import android.os.SystemClock
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.petesreps.data.ExerciseGuides
import org.petesreps.data.TrainingBackup
import org.petesreps.data.TrainingBackupCodec
import org.petesreps.data.TrainingDatabase
import org.petesreps.engine.SubstitutionEngine
import org.petesreps.engine.WorkoutEngine
import org.petesreps.model.ExercisePrescription
import org.petesreps.model.MeasureUnit
import org.petesreps.model.TrainingSummary
import org.petesreps.model.Workout
import org.petesreps.session.ActivePrescriptionState
import org.petesreps.session.ActiveSessionRun
import org.petesreps.session.SessionRunStore
import org.petesreps.session.SessionTiming

@Composable
fun PetesRepsScreen(database: TrainingDatabase, engine: WorkoutEngine) {
    val context = LocalContext.current
    val runStore = remember { SessionRunStore(context.applicationContext) }
    val substitutionEngine = remember { SubstitutionEngine() }
    var historyRevision by remember { mutableIntStateOf(0) }
    var workout by remember(historyRevision) {
        val generated = engine.generate(database.profile())
        val savedRun = runStore.load(generated.dayNumber)
        mutableStateOf(restoreActiveWorkout(generated, savedRun, substitutionEngine))
    }
    var summary by remember(historyRevision) { mutableStateOf(database.summary()) }
    var completionMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<TrainingBackup?>(null) }
    val restoredRun = remember(workout.dayNumber, historyRevision) { runStore.load(workout.dayNumber) }
    val actuals = remember(workout.dayNumber, historyRevision) {
        mutableStateMapOf<String, Int>().apply { putAll(restoredRun?.actuals.orEmpty()) }
    }
    val rejectedByBlock = remember(workout.dayNumber, historyRevision) {
        mutableStateMapOf<Int, Set<String>>()
    }
    var started by rememberSaveable(workout.dayNumber, historyRevision) {
        mutableStateOf(restoredRun != null)
    }
    var showOverview by rememberSaveable(workout.dayNumber, historyRevision) { mutableStateOf(false) }
    var currentIndex by rememberSaveable(workout.dayNumber, historyRevision) {
        mutableIntStateOf(
            restoredRun?.currentIndex
                ?.coerceIn(0, workout.prescriptions.lastIndex.coerceAtLeast(0))
                ?: 0,
        )
    }
    var sessionStartedElapsedMillis by rememberSaveable(workout.dayNumber, historyRevision) {
        mutableLongStateOf(restoredRun?.sessionStartedElapsedMillis ?: 0L)
    }
    var blockStartedElapsedMillis by rememberSaveable(workout.dayNumber, historyRevision) {
        mutableLongStateOf(restoredRun?.blockStartedElapsedMillis ?: 0L)
    }
    var nowElapsedMillis by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

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

    fun prescriptionStates(prescriptions: List<ExercisePrescription>): List<ActivePrescriptionState> =
        prescriptions.map { prescription ->
            ActivePrescriptionState(
                exerciseId = prescription.exercise.id,
                sets = prescription.sets,
                targetPerSet = prescription.targetPerSet,
                blockMinutes = prescription.blockMinutes,
                stimulusFamilyName = prescription.stimulusFamily.name,
            )
        }

    fun saveRun(prescriptions: List<ExercisePrescription> = workout.prescriptions) {
        if (!started || sessionStartedElapsedMillis <= 0L) return
        runStore.save(
            ActiveSessionRun(
                dayNumber = workout.dayNumber,
                sessionStartedElapsedMillis = sessionStartedElapsedMillis,
                blockStartedElapsedMillis = blockStartedElapsedMillis,
                currentIndex = currentIndex,
                actuals = actuals.toMap(),
                prescriptions = prescriptionStates(prescriptions),
            )
        )
    }

    fun completeWorkout(hardStop: Boolean = false) {
        val savedDay = workout.dayNumber
        val workoutToSave = if (hardStop) {
            workout.copy(
                prescriptions = workout.prescriptions.filter { actuals.containsKey(it.exercise.id) }
            )
        } else {
            workout
        }
        database.completeWorkout(workoutToSave, actuals.toMap())
        runStore.clear()
        summary = database.summary()
        workout = engine.generate(database.profile())
        completionMessage = if (hardStop) {
            "25:00 reached. Session $savedDay stopped and saved."
        } else {
            "Session $savedDay saved."
        }
        started = false
        showOverview = false
        currentIndex = 0
        sessionStartedElapsedMillis = 0L
        blockStartedElapsedMillis = 0L
    }

    fun moveTo(index: Int) {
        currentIndex = index.coerceIn(0, workout.prescriptions.lastIndex.coerceAtLeast(0))
        blockStartedElapsedMillis = SystemClock.elapsedRealtime()
        nowElapsedMillis = blockStartedElapsedMillis
        saveRun()
    }

    fun swapAt(index: Int) {
        if (index !in workout.prescriptions.indices) return
        val current = workout.prescriptions[index]
        val alreadyRejected = rejectedByBlock[index].orEmpty()
        val avoid = workout.prescriptions.map { it.exercise.id }.toSet() + alreadyRejected
        val replacement = substitutionEngine.substitute(
            current = current,
            avoidExerciseIds = avoid - current.exercise.id,
        )
        if (replacement == null) {
            completionMessage = "No useful substitute is available for this movement."
            return
        }

        actuals.remove(current.exercise.id)
        rejectedByBlock[index] = alreadyRejected + current.exercise.id
        val replacements = workout.prescriptions.toMutableList().apply { set(index, replacement) }
        workout = workout.copy(prescriptions = replacements)
        saveRun(replacements)
    }

    LaunchedEffect(started, sessionStartedElapsedMillis, workout.dayNumber) {
        if (!started || sessionStartedElapsedMillis <= 0L) return@LaunchedEffect
        while (true) {
            val now = SystemClock.elapsedRealtime()
            nowElapsedMillis = now
            if (SessionTiming.isExpired(sessionStartedElapsedMillis, now)) {
                completeWorkout(hardStop = true)
                break
            }
            delay(250L)
        }
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
                                runStore.clear()
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
                        "Session ${workout.dayNumber} • ${workout.cycleDay} of 6 • ${workout.plannedMinutes} min programmed",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    completionMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }

                if (started) {
                    item {
                        SessionClockBanner(
                            SessionTiming.remainingMillis(
                                sessionStartedElapsedMillis,
                                nowElapsedMillis,
                            )
                        )
                    }
                }

                if (!started || showOverview) {
                    item {
                        Text(
                            if (started) "Session overview" else "Today's session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    itemsIndexed(
                        items = workout.prescriptions,
                        key = { index, prescription -> "overview_${index}_${prescription.exercise.id}" },
                    ) { index, prescription ->
                        OverviewCard(
                            prescription = prescription,
                            onSwap = { swapAt(index) },
                        )
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
                                    val now = SystemClock.elapsedRealtime()
                                    started = true
                                    showOverview = false
                                    currentIndex = 0
                                    sessionStartedElapsedMillis = now
                                    blockStartedElapsedMillis = now
                                    nowElapsedMillis = now
                                    actuals.clear()
                                    runStore.save(
                                        ActiveSessionRun(
                                            dayNumber = workout.dayNumber,
                                            sessionStartedElapsedMillis = now,
                                            blockStartedElapsedMillis = now,
                                            currentIndex = 0,
                                            actuals = emptyMap(),
                                            prescriptions = prescriptionStates(workout.prescriptions),
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Start 25:00 session")
                            }
                        }
                    }
                } else {
                    val prescription = workout.prescriptions[currentIndex]
                    val blockRemainingMillis = SessionTiming.blockRemainingMillis(
                        blockStartedAtElapsedMillis = blockStartedElapsedMillis,
                        nowElapsedMillis = nowElapsedMillis,
                        blockMinutes = prescription.blockMinutes,
                    )
                    item {
                        Text(
                            "Movement ${currentIndex + 1} of ${workout.prescriptions.size}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.height(8.dp))
                        BlockClockBanner(blockRemainingMillis)
                        Spacer(Modifier.height(8.dp))
                        ExerciseCard(
                            prescription = prescription,
                            actual = actuals[prescription.exercise.id] ?: 0,
                            onActualChange = { value ->
                                actuals[prescription.exercise.id] = value.coerceAtLeast(0)
                                saveRun()
                            },
                            onSwap = { swapAt(currentIndex) },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { moveTo(currentIndex - 1) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Previous") }
                            }
                            if (currentIndex < workout.prescriptions.lastIndex) {
                                Button(
                                    onClick = { moveTo(currentIndex + 1) },
                                    modifier = Modifier.weight(1f),
                                ) { Text(if (blockRemainingMillis == 0L) "Move on" else "Next") }
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
                                onClick = { completeWorkout() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Complete session")
                            }
                        }
                    }
                }

                if (!started) {
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
                } else {
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

private fun restoreActiveWorkout(
    generated: Workout,
    run: ActiveSessionRun?,
    substitutionEngine: SubstitutionEngine,
): Workout {
    if (run == null || run.prescriptions.size != generated.prescriptions.size) return generated
    val restored = generated.prescriptions.mapIndexed { index, fallback ->
        val state = run.prescriptions[index]
        substitutionEngine.restore(
            exerciseId = state.exerciseId,
            sets = state.sets,
            targetPerSet = state.targetPerSet,
            blockMinutes = state.blockMinutes,
            stimulusFamilyName = state.stimulusFamilyName,
        ) ?: fallback
    }
    return generated.copy(
        prescriptions = restored,
        plannedMinutes = restored.sumOf { it.blockMinutes }.coerceAtMost(25),
    )
}

@Composable
fun SessionClockBanner(remainingMillis: Long) {
    Card(modifier = Modifier.fillMaxWidth().testTag("session-clock")) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("SESSION TIME LEFT", style = MaterialTheme.typography.labelLarge)
            Text(
                SessionTiming.formatClock(remainingMillis),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text("At 00:00, the session stops.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun BlockClockBanner(remainingMillis: Long) {
    Card(modifier = Modifier.fillMaxWidth().testTag("block-clock")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("MOVEMENT TIME", style = MaterialTheme.typography.labelMedium)
            Text(
                if (remainingMillis == 0L) "MOVE ON" else SessionTiming.formatClock(remainingMillis),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OverviewCard(
    prescription: ExercisePrescription,
    onSwap: () -> Unit,
) {
    var showHow by rememberSaveable(prescription.exercise.id) { mutableStateOf(false) }
    val unit = if (prescription.exercise.unit == MeasureUnit.REPS) "reps" else "sec"
    val side = if (prescription.exercise.perSide) " / side" else ""
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { showHow = !showHow },
                    modifier = Modifier.weight(1f).testTag("how-${prescription.exercise.id}"),
                ) {
                    Text(if (showHow) "Hide how" else "How")
                }
                OutlinedButton(
                    onClick = onSwap,
                    modifier = Modifier.weight(1f).testTag("swap-${prescription.exercise.id}"),
                ) {
                    Text("Swap")
                }
            }
            if (showHow) {
                Text(ExerciseGuides.descriptionFor(prescription.exercise), style = MaterialTheme.typography.bodyMedium)
                prescription.exercise.cues.forEach { cue ->
                    Text("• $cue", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    prescription: ExercisePrescription,
    actual: Int,
    onActualChange: (Int) -> Unit,
    onSwap: () -> Unit,
) {
    var showHow by rememberSaveable(prescription.exercise.id) { mutableStateOf(false) }
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
            OutlinedButton(
                onClick = onSwap,
                modifier = Modifier.fillMaxWidth().testTag("swap-current"),
            ) {
                Text("Swap")
            }
            OutlinedButton(
                onClick = { showHow = !showHow },
                modifier = Modifier.fillMaxWidth().testTag("how-current"),
            ) {
                Text(if (showHow) "Hide how" else "How")
            }
            if (showHow) {
                Text(ExerciseGuides.descriptionFor(prescription.exercise), style = MaterialTheme.typography.bodyMedium)
                prescription.exercise.cues.forEach { cue ->
                    Text("• $cue", style = MaterialTheme.typography.bodyMedium)
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
