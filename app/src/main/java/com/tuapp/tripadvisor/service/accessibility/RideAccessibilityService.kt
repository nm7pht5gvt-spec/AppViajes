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
        val rootNode = rootInActiveWindow ?: event?.source ?: return

        val screenTextBuilder = StringBuilder()
        collectDeepText(rootNode, screenTextBuilder)
        val textToParse = screenTextBuilder.toString()

        if (textToParse.isBlank()) return

        // 1. Verificar si el conductor ya va en viaje
        if (ScreenTextParser.isCurrentlyInTrip(textToParse)) {
            OverlayService.updateEvaluation(applicationContext, TripEvaluation.InTrip)
            return
        }

        // 2. Analizar oferta (DiDi / Uber / InDrive / Centro de Viajes)
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

    private fun collectDeepText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        // Textos normales
        if (!node.text.isNullOrEmpty()) {
            builder.append(node.text).append(" ")
        }
        // Descripciones para lectores de pantalla (DiDi usa esto en montos y distancias)
        if (!node.contentDescription.isNullOrEmpty()) {
            builder.append(node.contentDescription).append(" ")
        }
        // Hint text en campos de formulario
        if (!node.hintText.isNullOrEmpty()) {
            builder.append(node.hintText).append(" ")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                collectDeepText(child, builder)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {}
}
