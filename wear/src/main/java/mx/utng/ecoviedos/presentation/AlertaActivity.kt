package mx.utng.ecoviedos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import mx.utng.ecoviedos.presentation.theme.AppTheme

class AlertaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parcela = intent.getStringExtra("parcela") ?: "Parcela 7"
        val variedad = intent.getStringExtra("variedad") ?: "Cabernet Sauvignon"
        val humedad = intent.getStringExtra("humedad") ?: "Humedad crítica"

        setContent {
            AppTheme {
                TransformingLazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    item {
                        Text(
                            text = "ALERTA DE RIEGO",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        Card(
                            onClick = { }
                        ) {

                            Text("Parcela: $parcela")
                            Text("Variedad: $variedad")
                            Text("Estado: $humedad")

                        }
                    }

                }
            }
        }
    }
}