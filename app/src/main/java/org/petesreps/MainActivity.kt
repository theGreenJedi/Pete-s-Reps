package org.petesreps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import org.petesreps.data.TrainingDatabase
import org.petesreps.engine.WorkoutEngine
import org.petesreps.ui.PetesRepsScreen

class MainActivity : ComponentActivity() {
    private lateinit var database: TrainingDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = TrainingDatabase(applicationContext)
        val engine = WorkoutEngine()
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                PetesRepsScreen(database = database, engine = engine)
            }
        }
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
