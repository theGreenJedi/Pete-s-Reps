package org.petesreps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
            PetesRepsScreen(database = database, engine = engine)
        }
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
