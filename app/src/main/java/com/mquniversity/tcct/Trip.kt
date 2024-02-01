package com.mquniversity.tcct

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// https://developer.android.com/training/data-storage/room
@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val date: Date,
    val origin: String,
    val destination: String,
    val distance: Long,
    val vehicle: String,
    val fuel: String,
    val emissions: Float
)
