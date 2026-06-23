package dev.anilbeesetti.nextplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.anilbeesetti.nextplayer.core.database.entities.FolderIntroEntity
import dev.anilbeesetti.nextplayer.core.database.entities.MediumStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediumStateDao {

    @Upsert
    suspend fun upsert(mediumState: MediumStateEntity)

    @Upsert
    suspend fun upsertAll(mediaStates: List<MediumStateEntity>)

    @Query("SELECT * FROM media_state WHERE uri = :uri")
    suspend fun get(uri: String): MediumStateEntity?

    @Query("SELECT * FROM media_state WHERE uri = :uri")
    fun getAsFlow(uri: String): Flow<MediumStateEntity?>

    @Query("SELECT * FROM media_state")
    fun getAll(): Flow<List<MediumStateEntity>>

    @Query("DELETE FROM media_state WHERE uri in (:uris)")
    suspend fun delete(uris: List<String>)

    @Upsert
    suspend fun upsertFolderIntro(folderIntro: FolderIntroEntity)

    @Query("SELECT * FROM folder_intro WHERE folder_path = :folderPath")
    suspend fun getFolderIntro(folderPath: String): FolderIntroEntity?

    @Query("DELETE FROM folder_intro WHERE folder_path = :folderPath")
    suspend fun deleteFolderIntro(folderPath: String)
}
