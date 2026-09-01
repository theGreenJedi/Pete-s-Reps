package org.petesreps.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.petesreps.data.TrainingDatabase
import org.petesreps.engine.WorkoutEngine
import org.petesreps.model.ExercisePrescription
import org.petesreps.model.MeasureUnit
import org.petesreps.model.TrainingSummary
import org.petesreps.model.Workout

@Composable
fun PetesRepsScreen(database: TrainingDatabase, engine: WorkoutEngine) {
    var workout by remember { mutableStateOf(engine.generate(database.profile())) }
    var summary by remember { mutableStateOf(database.summary()) }
    var completionMessage by remember { mutableStateOf<String?>(null) }
    val actuals = remember(workout.dayNumber) { mutableMapOf<String, Int>() }
    var editVersion by remember(workout.dayNumber) { mutableIntStateOf(0) }

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
                        "Day ${workout.dayNumber} • Cycle ${workout.cycleNumber}, day ${workout.cycleDay} • ${workout.plannedMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(workout.focus, style = MaterialTheme.typography.titleMedium)
                    completionMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }

                items(workout.prescriptions, key = { it.exercise.id }) { prescription ->
                    val ignored = editVersion
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
                    HorizontalDivider()
                    Button(
                        onClick = {
                            val savedDay = workout.dayNumber
                            database.completeWorkout(workout, actuals.toMap())
                            summary = database.summary()
                            workout = engine.generate(database.profile())
                            completionMessage = "Day $savedDay saved."
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Complete today's workout")
                    }
                    Spacer(Modifier.height(8.dp))
                    Summary(summary)
                    Spacer(Modifier.height(24.dp))
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
) {
    var showCues by rememberSaveable(prescription.exercise.id) { mutableStateOf(false) }
    val unit = if (prescription.exercise.unit == MeasureUnit.REPS) "reps" else "sec"
    val side = if (prescription.exercise.perSide) " / side" else ""
    val increment = if (prescription.exercise.unit == MeasureUnit.SECONDS) 5 else 1

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(prescription.exercise.family.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
            Text(prescription.exercise.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${prescription.sets} × ${prescription.targetPerSet} $unit$side • ${prescription.blockMinutes}-minute block")
            prescription.challengeNote?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

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
    Text("Tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("${summary.workoutsLogged} workouts logged • next day ${summary.currentDay}")
    Text(
        summary.challengeByFamily.entries.joinToString("  ") { (family, challenge) ->
            "${family.name.take(3)} $challenge"
        },
        style = MaterialTheme.typography.bodySmall,
    )
}
