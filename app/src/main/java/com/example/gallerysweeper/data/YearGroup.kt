package com.example.gallerysweeper.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class YearGroup(
    val year: Int,
    val months: List<MonthGroup>
)
@Parcelize
data class MonthGroup(
    val month: Int,
    val items: List<MediaItem>
): Parcelable
