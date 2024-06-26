package com.example.gallerysweeper

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallerysweeper.data.MediaItem
import com.example.gallerysweeper.data.MonthGroup
import com.example.gallerysweeper.data.YearGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainViewModel(): ViewModel() {
    private val _permissionStateRead = MutableLiveData<Boolean>(false)
    val permissionStateRead: LiveData<Boolean> get() = _permissionStateRead

    private val _allMediaItems = MutableLiveData<List<MediaItem>>()
    val allMediaItems : LiveData<List<MediaItem>> get() = _allMediaItems

    private val _allPhotos = MutableLiveData<List<MediaItem>>()
    val allPhotos : LiveData<List<MediaItem>> get() = _allPhotos

    private val _allVideos = MutableLiveData<List<MediaItem>>()
    val allVideos : LiveData<List<MediaItem>> get() = _allVideos

    private val _allScreenshots = MutableLiveData<List<MediaItem>>()
    val allScreenshots : LiveData<List<MediaItem>> get() = _allScreenshots

    private val _groupedMediaItems = MutableLiveData<List<YearGroup>>()
    val groupedMediaItems: LiveData<List<YearGroup>> get() = _groupedMediaItems



    fun givePermissionRead(){
        _permissionStateRead.value = true
    }

    fun takePermissionRead(){
        _permissionStateRead.value = false
    }

    fun setPermissionState(state:Boolean){
        _permissionStateRead.value = state
    }


    fun getMediaItems(context: Context) {
        viewModelScope.launch (Dispatchers.IO){
            val allMediaItems = mutableListOf<MediaItem>()
            val photoItems = mutableListOf<MediaItem>()
            val videoItems = mutableListOf<MediaItem>()
            val screenshotItems = mutableListOf<MediaItem>()

            val projection = arrayOf(
             MediaStore.MediaColumns._ID,
             MediaStore.MediaColumns.DISPLAY_NAME,
             MediaStore.MediaColumns.SIZE,
             MediaStore.MediaColumns.DATE_ADDED,
             MediaStore.MediaColumns.MIME_TYPE
            )


            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
         } else {
                "${MediaStore.MediaColumns.DATA} LIKE ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
            }

         val selectionArgs = arrayOf(
              "%DCIM%",
             "%Download%"
         )

         val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            listOf(imageUri, videoUri).forEach { uri ->
                context?.contentResolver?.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val dateAddedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val size = cursor.getLong(sizeColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        val mimeType = cursor.getString(mimeTypeColumn)

                        val contentUri = ContentUris.withAppendedId(
                            if (mimeType.startsWith("video")) videoUri else imageUri,
                            id
                        )

                        val mediaItem = MediaItem(
                            id = id,
                            uri = contentUri,
                            name = name,
                            size = size,
                            dateAdded = dateAdded,
                            isVideo = mimeType.startsWith("video")
                        )

                        allMediaItems.add(mediaItem)

                        when{
                            mediaItem.isVideo -> videoItems.add(mediaItem)
                            isScreenshot(mediaItem.name) -> screenshotItems.add(mediaItem)
                            else -> photoItems.add(mediaItem)
                        }
                    }
                }
        }


             withContext(Dispatchers.Main) {
                 _allMediaItems.value = allMediaItems
                 _allPhotos.value = photoItems
                 _allVideos.value = videoItems
                 _allScreenshots.value = screenshotItems
            }
        }


    }

    fun getMediaItemsByYearAndMonth(mediaItems: List<MediaItem>): List<YearGroup> {
        return mediaItems.groupBy { item ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = item.dateAdded * 1000
            }
            calendar.get(Calendar.YEAR)
        }.map { (year, itemsInYear) ->
            YearGroup(
                year = year,
                months = itemsInYear.groupBy { item ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = item.dateAdded * 1000
                    }
                    calendar.get(Calendar.MONTH)
                }.map { (month, itemsInMonth) ->
                    MonthGroup(month = month, items = itemsInMonth.sortedByDescending { it.dateAdded })
                }.sortedByDescending { it.month }
            )
        }.sortedByDescending { it.year }
    }

    fun updateGroupedMediaItems() {
        viewModelScope.launch {
            val grouped = getMediaItemsByYearAndMonth(_allMediaItems.value ?: emptyList())
            _groupedMediaItems.postValue(grouped)
        }
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        return monthNames[month]
    }

    private fun isScreenshot(fileName: String): Boolean {
        //todo check later how to find screenshots
        return fileName.lowercase().contains("screenshot")
    }

    fun bytesToGB(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }
}