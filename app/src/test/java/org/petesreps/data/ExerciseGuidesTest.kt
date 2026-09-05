package org.petesreps.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGuidesTest {
    @Test
    fun everyCatalogExerciseHasAPlainLanguageGuide() {
        val missing = ExerciseCatalog.all.filterNot { ExerciseGuides.hasGuide(it.id) }
        assertTrue("Missing movement guides: ${missing.joinToString { it.id }}", missing.isEmpty())
    }

    @Test
    fun deadBugGuideExplainsSetupAndAlternatingMotion() {
        val deadBug = ExerciseCatalog.all.single { it.id == "dead_bug" }
        val guide = ExerciseGuides.descriptionFor(deadBug).lowercase()

        assertTrue(guide.contains("back"))
        assertTrue(guide.contains("arm"))
        assertTrue(guide.contains("leg"))
        assertTrue(guide.contains("switch"))
    }
}
