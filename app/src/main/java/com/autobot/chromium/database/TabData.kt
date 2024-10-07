package com.autobot.chromium.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String
)