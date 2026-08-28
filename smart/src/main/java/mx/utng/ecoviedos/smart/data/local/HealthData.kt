package mx.utng.ecoviedos.smart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis()
)
