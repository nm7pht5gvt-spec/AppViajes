package com.tuapp.tripadvisor.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tuapp.tripadvisor.data.preferences.PreferencesRepository
import com.tuapp.tripadvisor.domain.model.TripEvaluation
import com.tuapp.tripadvisor.domain.model.TripEvaluator
import com.tuapp.tripadvisor.service.overlay.OverlayService
import com.tuapp.tripadvisor.util.ScreenTextParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RideAccessibilityService : AccessibilityService() {

    private val evaluator = TripEvaluator()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        val screenTextBuilder = StringBuilder()
        collectText(rootNode, screenTextBuilder)
        val textToParse = screenTextBuilder.toString()

        // 1. Detectar si el usuario ya está en viaje
        if (ScreenTextParser.isCurrentlyInTrip(textToParse)) {
            OverlayService.updateEvaluation(applicationContext, TripEvaluation.InTrip)
            return
        }

        // 2. Intentar parsear oferta de viaje (Uber, DiDi, InDrive)
        val offer = ScreenTextParser.parse(textToParse)
        if (offer != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val repository = PreferencesRepository(applicationContext)
                val prefs = repository.userPreferencesFlow.first()
                val evaluation = evaluator.evaluate(offer, prefs, textToParse)
                OverlayService.updateEvaluation(applicationContext, evaluation)
            }
        } else {
            // Si la oferta fue aceptada/rechazada y desaparece de la pantalla, reiniciar a Idle
            OverlayService.updateEvaluation(applicationContext, TripEvaluation.Idle)
        }
    }

    private fun collectText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return
        if (!node.text.isNullOrEmpty()) {
            builder.append(node.text).append(" ")
        }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), builder)
        }
    }

    override fun onInterrupt() {}
}
