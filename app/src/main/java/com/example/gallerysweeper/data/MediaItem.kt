package com.example.gallerysweeper.data

import android.net.Uri

data class MediaItem(
    val id : Long,
    val uri :Uri,
    val name : String,
    val size : Long,
    val dateAdded : Long,
    val isVideo : Boolean
)
