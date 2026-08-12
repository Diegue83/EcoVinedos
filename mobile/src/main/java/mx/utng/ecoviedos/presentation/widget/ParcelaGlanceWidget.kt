package mx.utng.ecoviedos.presentation.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import mx.utng.ecoviedos.MainActivity
import mx.utng.ecoviedos.data.local.SessionManager
import mx.utng.ecoviedos.data.repository.ParcelaRepository
import mx.utng.ecoviedos.domain.model.Parcela
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.GlanceAppWidgetManager

class ParcelaGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val sessionManager = SessionManager(context)
        val token = sessionManager.token.first()
        val repo = ParcelaRepository()
        
        // Obtener el ID numérico del widget para la configuración
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val parcelId = prefs[stringPreferencesKey("parcel_id")]
            val parcelName = prefs[stringPreferencesKey("parcel_name")] ?: "Seleccionar Parcela"
            val transparency = prefs[floatPreferencesKey("transparency")] ?: 0.7f
            val riegoTime = prefs[intPreferencesKey("riego_time")] ?: 5

            var parcelaData by remember { mutableStateOf<Parcela?>(null) }

            LaunchedEffect(parcelId) {
                if (parcelId != null && token != null) {
                    repo.obtenerParcelas(token).onSuccess { list ->
                        parcelaData = list.find { it.id == parcelId }
                    }
                }
            }

            WidgetContent(parcelName, parcelaData, transparency, riegoTime, appWidgetId)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(name: String, data: Parcela?, transparency: Float, riegoTime: Int, appWidgetId: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ColorProvider(Color.Black.copy(alpha = transparency)))
                .cornerRadius(16.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con nombre y engranaje
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    modifier = GlanceModifier.defaultWeight().clickable(actionStartActivity<MainActivity>()),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ColorProvider(Color.White)
                    )
                )
                
                // Engranaje para configurar - Acción corregida
                Box(
                    modifier = GlanceModifier
                        .padding(4.dp)
                        .clickable(actionStartActivity<WidgetConfigurationActivity>(
                            actionParametersOf(
                                ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID) to appWidgetId
                            )
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            if (data != null) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoItem(
                        label = "Suelo",
                        value = "${data.humedadSuelo.toInt()}%",
                        icon = getHumedadSueloIcon(data.humedadSuelo)
                    )
                    
                    InfoItem(
                        label = "Ambiente",
                        value = "${data.humedad.toInt()}%",
                        icon = getHumedadAireIcon(data.humedad)
                    )
                    
                    InfoItem(
                        label = "Temp",
                        value = "${data.temperatura.toInt()}°C",
                        icon = getTempIcon(data.temperatura)
                    )
                }

                Spacer(GlanceModifier.height(12.dp))

                val statusColor = if (data.riegoActivo) Color(0xFF4CAF50) else Color(0xFFF44336)
                
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Riego (${riegoTime}m)",
                            style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.LightGray))
                        )
                        Text(
                            text = if (data.riegoActivo) "ACTIVO" else "INACTIVO",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ColorProvider(statusColor)
                            )
                        )
                    }

                    Button(
                        text = if (data.riegoActivo) "Detener" else "Activar",
                        onClick = actionRunCallback<ToggleRiegoAction>(
                            actionParametersOf(
                                ActionParameters.Key<Boolean>("riego_status") to data.riegoActivo,
                                ActionParameters.Key<Int>("riego_time") to riegoTime
                            )
                        ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(if (data.riegoActivo) Color.DarkGray else Color(0xFF2E7D32)),
                            contentColor = ColorProvider(Color.White)
                        )
                    )
                }
            } else {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<WidgetConfigurationActivity>(
                        actionParametersOf(
                            ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID) to appWidgetId
                        )
                    )),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Configurar Parcela",
                        style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White))
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun RowScope.InfoItem(label: String, value: String, icon: String) {
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = TextStyle(fontSize = 24.sp)
            )
            Text(
                text = value, 
                style = TextStyle(
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = ColorProvider(Color.White)
                )
            )
            Text(
                text = label, 
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.LightGray))
            )
        }
    }

    private fun getHumedadSueloIcon(level: Float): String {
        return when {
            level < 20f -> "🏜️"
            level < 40f -> "🪴"
            level < 70f -> "💧"
            else -> "🌊"
        }
    }

    private fun getHumedadAireIcon(level: Float): String {
        return when {
            level < 30f -> "🌵"
            level < 60f -> "🍃"
            else -> "🌫️"
        }
    }

    private fun getTempIcon(temp: Float): String {
        return when {
            temp < 15f -> "❄️"
            temp < 28f -> "☀️"
            else -> "🔥"
        }
    }
}
