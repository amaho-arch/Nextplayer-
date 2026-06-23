package dev.anilbeesetti.nextplayer.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_intro")
data class FolderIntroEntity(
    @PrimaryKey
    @ColumnInfo(name = "folder_path")
    val folderPath: String,
    @ColumnInfo(name = "intro_start")
    val introStartMs: Long,
    @ColumnInfo(name = "intro_end")
    val introEndMs: Long,
)