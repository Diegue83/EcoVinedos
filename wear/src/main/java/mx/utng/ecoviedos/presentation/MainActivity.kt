package mx.utng.ecoviedos.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import mx.utng.ecoviedos.presentation.theme.EcoVinedosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EcoVinedosTheme {
                AppScaffold {
                    ScreenScaffold {
                        val context = LocalContext.current

                        Button(
                            onClick = {
                                val intent = Intent(context, AlertaActivity::class.java)
                                intent.putExtra("parcela", "Parcela 7")
                                intent.putExtra("variedad", "Cabernet Sauvignon")
                                intent.putExtra("humedad", "Humedad crítica")
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver alerta")
                        }
                    }
                }
            }
        }
    }
}