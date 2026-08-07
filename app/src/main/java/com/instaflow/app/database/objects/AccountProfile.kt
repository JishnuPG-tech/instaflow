package com.instaflow.app.database.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class AccountProfile(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val url: String,
    val content: String,
)
