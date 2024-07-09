package com.example.gallerysweeper

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gallerysweeper.adapters.DeleteAdapter
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

    private val _itemsToDelete = MutableLiveData<List<MediaItem>>()
    val itemsToDelete: LiveData<List<MediaItem>> get() = _itemsToDelete

    private val _currentCardPosition = MutableLiveData<Int>(0)
    val currentCardPosition: LiveData<Int> = _currentCardPosition

    private val _groupedType = MutableLiveData<String>()
    val groupedType: LiveData<String> get() = _groupedType

    private val _checkedItemsToDelete = MutableLiveData<Set<MediaItem>>(setOf())
    val checkedItemsToDelete: LiveData<Set<MediaItem>> = _checkedItemsToDelete

    private val _deletionComplete = MutableLiveData<Boolean>()
    val deletionComplete: LiveData<Boolean> get() = _deletionComplete

    fun setDeletionCompleteValue(value: Boolean){
        _deletionComplete.value = value
    }


    fun setCheckedItemToDelete(list: MutableSet<MediaItem>) {
        _checkedItemsToDelete.postValue(list)
    }

    fun removeCheckedItemToDelete(item: MediaItem) {
        val currentSet = _checkedItemsToDelete.value ?: setOf()
        _checkedItemsToDelete.value = currentSet - item
    }

    fun clearCheckedItemsToDelete() {
        _checkedItemsToDelete.value = setOf()
    }


    fun updateGroupedTypeValue(type:String){
        _groupedType.value = type
    }


    fun setCurrentCardPosition(position: Int) {
        _currentCardPosition.value = position
    }

    fun decreaseCurrentCardPosition(){
        val currentPosition = _currentCardPosition.value ?: 0
        if(currentPosition != 0){
            _currentCardPosition.value = currentPosition - 1
        }

    }

    fun resetCurrentCardPosition(){
        _currentCardPosition.value = 0
    }
    fun addSwipedItem(item: MediaItem) {
        val currentList = _itemsToDelete.value ?: listOf()
        if(!currentList.contains(item)){
            _itemsToDelete.value = currentList + item
        }
        else{
            Log.d("MainViewModel","Item already in the swipedList")
        }
    }

    fun removeSwipedItem(item:MediaItem){
        val currentList = _itemsToDelete.value ?: listOf()
        val updatedList = currentList.filter{it != item}
        _itemsToDelete.value = updatedList
    }

    fun removeAllSwipedItems(){
        _itemsToDelete.value = emptyList()
    }


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
        viewModelScope.launch(Dispatchers.IO) {
            val allMediaItems = mutableListOf<MediaItem>()
            val photoItems = mutableListOf<MediaItem>()
            val videoItems = mutableListOf<MediaItem>()
            val screenshotItems = mutableListOf<MediaItem>()

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH
            )

            val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            listOf(imageUri, videoUri).forEach { uri ->
                context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                    val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn)
                        val size = cursor.getLong(sizeColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        val mimeType = cursor.getString(mimeTypeColumn)
                        val relativePath = cursor.getString(relativePathColumn)

                        val contentUri = ContentUris.withAppendedId(uri, id)

                        if (relativePath.contains("DCIM") ||
                            relativePath.contains("Pictures") ||
                            relativePath.contains("Download") ||
                            relativePath.contains("Movies")) {

                            val mediaItem = MediaItem(
                                id = id,
                                uri = contentUri,
                                name = name,
                                size = size,
                                dateAdded = dateAdded,
                                isVideo = mimeType.startsWith("video")
                            )

                            allMediaItems.add(mediaItem)

                            when {
                                mediaItem.isVideo -> videoItems.add(mediaItem)
                                isScreenshot(mediaItem.name) -> screenshotItems.add(mediaItem)
                                else -> photoItems.add(mediaItem)
                            }
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

//    fun getMediaItems(context: Context) {
//        viewModelScope.launch (Dispatchers.IO){
//            val allMediaItems = mutableListOf<MediaItem>()
//            val photoItems = mutableListOf<MediaItem>()
//            val videoItems = mutableListOf<MediaItem>()
//            val screenshotItems = mutableListOf<MediaItem>()
//
//            val projection = arrayOf(
//             MediaStore.MediaColumns._ID,
//             MediaStore.MediaColumns.DISPLAY_NAME,
//             MediaStore.MediaColumns.SIZE,
//             MediaStore.MediaColumns.DATE_ADDED,
//             MediaStore.MediaColumns.MIME_TYPE
//            )
//
//
//            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ? OR ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
//         } else {
//                "${MediaStore.MediaColumns.DATA} LIKE ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
//            }
//
//            val selectionArgs = arrayOf(
//                "%DCIM%",
//                "%Pictures%",
//                "%Download%",
//                "%Movies%"
//            )
//
//            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
//            } else {
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            }
//
//            val videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
//            } else {
//                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//            }
//
//            listOf(imageUri, videoUri).forEach { uri ->
//                context.contentResolver.query(
//                    uri,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
//                )?.use { cursor ->
//                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
//                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
//                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
//                    val dateAddedColumn =
//                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
//                    val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
//
//                    while (cursor.moveToNext()) {
//                        val id = cursor.getLong(idColumn)
//                        val name = cursor.getString(nameColumn)
//                        val size = cursor.getLong(sizeColumn)
//                        val dateAdded = cursor.getLong(dateAddedColumn)
//                        val mimeType = cursor.getString(mimeTypeColumn)
//
//                        val contentUri = ContentUris.withAppendedId(
//                            if (mimeType.startsWith("video")) videoUri else imageUri,
//                            id
//                        )
//
//                        val mediaItem = MediaItem(
//                            id = id,
//                            uri = contentUri,
//                            name = name,
//                            size = size,
//                            dateAdded = dateAdded,
//                            isVideo = mimeType.startsWith("video")
//                        )
//
//                        allMediaItems.add(mediaItem)
//
//                        when{
//                            mediaItem.isVideo -> videoItems.add(mediaItem)
//                            isScreenshot(mediaItem.name) -> screenshotItems.add(mediaItem)
//                            else -> photoItems.add(mediaItem)
//                        }
//                    }
//                }
//        }
//
//
//             withContext(Dispatchers.Main) {
//                 _allMediaItems.value = allMediaItems
//                 _allPhotos.value = photoItems
//                 _allVideos.value = videoItems
//                 _allScreenshots.value = screenshotItems
//            }
//        }
//
//
//    }

    fun deleteMediaItems(context: Context, groupType: String, deleteChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val itemsToDelete = if (deleteChecked) {
                _checkedItemsToDelete.value ?: setOf()
            } else {
                _itemsToDelete.value ?: listOf()
            }

            val deletedItems = mutableListOf<MediaItem>()

            itemsToDelete.forEach { mediaItem ->
                try {
                    val rowsDeleted = contentResolver.delete(mediaItem.uri, null, null)
                    if (rowsDeleted > 0) {
                        Log.d("MainViewModel", "Deleted: ${mediaItem.uri}")
                        deletedItems.add(mediaItem)
                    } else {
                        Log.d("MainViewModel", "Failed to delete: ${mediaItem.uri}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MainViewModel", "Error deleting: ${mediaItem.uri}")
                }
            }

            val remainingItems = _allMediaItems.value?.filter { it !in deletedItems } ?: listOf()

            withContext(Dispatchers.Main) {
                _allMediaItems.value = remainingItems
                _allPhotos.value = remainingItems.filter { !it.isVideo && !isScreenshot(it.name) }
                _allVideos.value = remainingItems.filter { it.isVideo }
                _allScreenshots.value = remainingItems.filter { isScreenshot(it.name) }

                if (deleteChecked) {
                    val updatedCheckedItems = (_checkedItemsToDelete.value ?: setOf()) - deletedItems.toSet()
                    _checkedItemsToDelete.value = updatedCheckedItems

                    val updatedItemsToDelete = (_itemsToDelete.value ?: listOf()) - deletedItems
                    _itemsToDelete.value = updatedItemsToDelete
                } else {
                    val updatedItemsToDelete = (_itemsToDelete.value ?: listOf()) - deletedItems
                    _itemsToDelete.value = updatedItemsToDelete
                }

                _deletionComplete.value = true
                updateGroupedMediaItems(groupType)
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

    fun updateGroupedMediaItems(type:String) {
        viewModelScope.launch {
            when(type){
                "AllMedia" ->   {val grouped = getMediaItemsByYearAndMonth(_allMediaItems.value ?: emptyList())
                _groupedMediaItems.postValue(grouped)}
                "AllPhotos" -> {val grouped = getMediaItemsByYearAndMonth(_allPhotos.value ?: emptyList())
                    _groupedMediaItems.postValue(grouped)}
                "AllVideos" -> {val grouped = getMediaItemsByYearAndMonth(_allVideos.value ?: emptyList())
                    _groupedMediaItems.postValue(grouped)}
                "AllScreenshots" -> {val grouped = getMediaItemsByYearAndMonth(_allScreenshots.value ?: emptyList())
                    _groupedMediaItems.postValue(grouped)}
                else -> Log.d("MainViewModel","updateGroupedMediaItems invalid string")
            }

        }
    }


    private fun isScreenshot(fileName: String): Boolean {
        return fileName.lowercase().contains("screenshot")
    }

    fun bytesToGB(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }
}