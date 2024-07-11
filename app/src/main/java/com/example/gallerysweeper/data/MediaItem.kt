package com.example.gallerysweeper.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaItem(
    val id : Long,
    val uri :Uri,
    val name : String,
    val size : Long,
    val dateAdded : Long,
    val isVideo : Boolean,
    val relativePath: String
): Parcelable
