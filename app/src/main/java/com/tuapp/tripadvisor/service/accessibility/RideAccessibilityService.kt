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
        collectAllText(rootNode, screenTextBuilder)
        val textToParse = screenTextBuilder.toString()

        if (textToParse.isBlank()) return

        // 1. Verificar si está en viaje
        if (ScreenTextParser.isCurrentlyInTrip(textToParse)) {
            OverlayService.updateEvaluation(applicationContext, TripEvaluation.InTrip)
            return
        }

        // 2. Intentar parsear oferta de viaje (DiDi / Uber / InDrive)
        val offer = ScreenTextParser.parse(textToParse)
        if (offer != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val repository = PreferencesRepository(applicationContext)
                val prefs = repository.userPreferencesFlow.first()
                val evaluation = evaluator.evaluate(offer, prefs, textToParse)
                OverlayService.updateEvaluation(applicationContext, evaluation)
            }
        }
    }

    private fun collectAllText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        // Extraer texto principal
        if (!node.text.isNullOrEmpty()) {
            builder.append(node.text).append(" ")
        }
        // Extraer descripción accesible (DiDi usa mucho esta propiedad en sus iconos e importes)
        if (!node.contentDescription.isNullOrEmpty()) {
            builder.append(node.contentDescription).append(" ")
        }

        for (i in 0 until node.childCount) {
            collectAllText(node.getChild(i), builder)
        }
    }

    override fun onInterrupt() {}
}
