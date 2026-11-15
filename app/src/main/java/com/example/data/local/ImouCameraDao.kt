package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ImouCameraDao {
    @Query("SELECT * FROM imou_cameras ORDER BY isFavorite DESC, id ASC")
    fun getAllCameras(): Flow<List<ImouCameraEntity>>

    @Query("SELECT COUNT(*) FROM imou_cameras")
    suspend fun getCameraCount(): Int

    @Query("SELECT * FROM imou_cameras WHERE id = :id LIMIT 1")
    suspend fun getCameraById(id: Long): ImouCameraEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamera(camera: ImouCameraEntity): Long

    @Update
    suspend fun updateCamera(camera: ImouCameraEntity)

    @Delete
    suspend fun deleteCamera(camera: ImouCameraEntity)

    @Query("SELECT * FROM ptz_presets WHERE cameraId = :cameraId ORDER BY presetNumber ASC")
    fun getPresetsForCamera(cameraId: Long): Flow<List<PtzPresetEntity>>

    @Query("SELECT * FROM ptz_presets ORDER BY cameraId ASC, presetNumber ASC")
    fun getAllPresets(): Flow<List<PtzPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PtzPresetEntity): Long

    @Query("DELETE FROM ptz_presets WHERE id = :presetId")
    suspend fun deletePreset(presetId: Long)

    @Query("SELECT * FROM vault_events ORDER BY timestamp DESC LIMIT 200")
    fun getAllVaultEvents(): Flow<List<VaultEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultEvent(event: VaultEventEntity): Long

    @Query("DELETE FROM vault_events WHERE id = :eventId")
    suspend fun deleteVaultEvent(eventId: Long)

    @Query("DELETE FROM vault_events")
    suspend fun clearAllVaultEvents()
}
