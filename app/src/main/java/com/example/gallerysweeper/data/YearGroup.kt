package com.example.gallerysweeper.data

data class YearGroup(
    val year: Int,
    val months: List<MonthGroup>
)

data class MonthGroup(
    val month: Int,
    val items: List<MediaItem>
)
