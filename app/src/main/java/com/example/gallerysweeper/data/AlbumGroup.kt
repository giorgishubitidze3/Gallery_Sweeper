package com.example.gallerysweeper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AlbumGroup(
    val name: String,
    val items: List<MediaItem>
): Parcelable
