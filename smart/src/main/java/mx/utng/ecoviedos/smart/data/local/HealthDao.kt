package mx.utng.ecoviedos.smart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_data ORDER BY timestamp DESC")
    fun getAllHealthData(): Flow<List<HealthData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthData(data: HealthData)

    @Query("DELETE FROM health_data")
    suspend fun deleteAll()
}
