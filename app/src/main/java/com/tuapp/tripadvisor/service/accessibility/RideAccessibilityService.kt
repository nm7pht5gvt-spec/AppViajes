package com.tuapp.tripadvisor.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tuapp.tripadvisor.data.preferences.PreferencesRepository
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

        // Extraer todo el texto visible en la pantalla
        val screenText = StringBuilder()
        collectText(rootNode, screenText)

        val textToParse = screenText.toString()

        // Intentar parsear el viaje
        val offer = ScreenTextParser.parse(textToParse)
        if (offer != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val repository = PreferencesRepository(applicationContext)
                val prefs = repository.userPreferencesFlow.first()
                val evaluation = evaluator.evaluate(offer, prefs)

                // Enviar el resultado al OverlayService
                OverlayService.updateEvaluation(applicationContext, evaluation)
            }
        }
    }

    private fun collectText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return
        if (node.text != null && node.text.isNotEmpty()) {
            builder.append(node.text).append(" ")
        }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), builder)
        }
    }

    override fun onInterrupt() {}
}
