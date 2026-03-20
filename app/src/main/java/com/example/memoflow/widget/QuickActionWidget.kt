package com.example.memoflow.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.memoflow.MainActivity
import com.example.memoflow.R

class QuickActionWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val pageIndex = prefs[PAGE_KEY] ?: 0
            WidgetLayout(pageIndex)
        }
    }

    @Composable
    private fun WidgetLayout(pageIndex: Int) {
        val widgetData = when (val idx = if (pageIndex < 0) (3 + (pageIndex % 3)) % 3 else pageIndex % 3) {
            1 -> WidgetPageData(R.drawable.ic_widget_gratitude_center, "gratitude")
            2 -> WidgetPageData(R.drawable.ic_widget_recall_center, "recall")
            else -> WidgetPageData(R.drawable.ic_widget_note_center, "write_note")
        }

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // CAMADA 0: GLOW DE FUNDO (Cobre tudo)
            Image(
                provider = ImageProvider(R.drawable.bg_widget_glow_neon_purple),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // CAMADA 1: ÍCONE TITÃ (Aumentado ao máximo e centralizado)
            Image(
                provider = ImageProvider(widgetData.iconRes),
                contentDescription = "Ação Principal",
                modifier = GlanceModifier.fillMaxSize().padding(5.dp),
                contentScale = ContentScale.Fit
            )

            // CAMADA 2: ÁREAS DE TOQUE GIGANTES (Invisíveis)
            Row(modifier = GlanceModifier.fillMaxSize()) {
                // Toque Esquerdo (Voltar) - 35% do Widget
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .defaultWeight()
                        .clickable(actionRunCallback<UpdatePageAction>(actionParametersOf(IncrementParamKey to -1)))
                ) {}

                // Toque Central (Ação) - 30% do Widget
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .clickable(actionStartActivity<MainActivity>(
                            actionParametersOf(ActionParameters.Key<String>("WIDGET_ROUTE") to widgetData.route)
                        ))
                ) {}

                // Toque Direito (Avançar) - 35% do Widget
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .defaultWeight()
                        .clickable(actionRunCallback<UpdatePageAction>(actionParametersOf(IncrementParamKey to 1)))
                ) {}
            }
        }
    }

    companion object {
        val PAGE_KEY = intPreferencesKey("widget_page_final_v5")
        val IncrementParamKey = ActionParameters.Key<Int>("inc_val")
    }
}

class UpdatePageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val increment = parameters[QuickActionWidget.IncrementParamKey] ?: 0
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[QuickActionWidget.PAGE_KEY] ?: 0
            prefs.toMutablePreferences().apply {
                this[QuickActionWidget.PAGE_KEY] = current + increment
            }
        }
        QuickActionWidget().updateAll(context) // Atualiza todos os widgets da marca
    }
}

data class WidgetPageData(val iconRes: Int, val route: String)

class QuickActionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionWidget()
}
