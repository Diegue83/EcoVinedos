package mx.utng.ecoviedos.presentation.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import mx.utng.ecoviedos.presentation.main.MainViewModel

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    
    private var mainViewModel: MainViewModel? = null

    fun setMainViewModel(viewModel: MainViewModel) {
        mainViewModel = viewModel
    }

    fun addParcel(nombre: String, variedad: String, area: Int, humedad: Float, temp: Float) {
        mainViewModel?.addNewParcel(nombre, variedad, area, humedad, temp)
    }

    fun addUser(nombre: String, correo: String, rol: String) {
        // Lógica de usuario (sin cambios para el test de parcelas)
    }
}
