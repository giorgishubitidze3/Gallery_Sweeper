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
import com.example.gallerysweeper.data.AlbumGroup
import com.example.gallerysweeper.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainViewModel : ViewModel() {
    private val _permissionStateRead = MutableLiveData<Boolean>(false)
    val permissionStateRead: LiveData<Boolean> get() = _permissionStateRead

    private val _allMediaItems = MutableLiveData<List<MediaItem>>()
    val allMediaItems: LiveData<List<MediaItem>> get() = _allMediaItems

    private val _groupedMediaItems = MutableLiveData<List<AlbumGroup>>()
    val groupedMediaItems: LiveData<List<AlbumGroup>> get() = _groupedMediaItems

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

    private val _allPhotos = MutableLiveData<List<MediaItem>>()
    val allPhotos: LiveData<List<MediaItem>> get() = _allPhotos

    private val _allVideos = MutableLiveData<List<MediaItem>>()
    val allVideos: LiveData<List<MediaItem>> get() = _allVideos

    private val _allScreenshots = MutableLiveData<List<MediaItem>>()
    val allScreenshots: LiveData<List<MediaItem>> get() = _allScreenshots

    fun setDeletionCompleteValue(value: Boolean) {
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

    fun updateGroupedTypeValue(type: String) {
        _groupedType.value = type
    }

    fun setCurrentCardPosition(position: Int) {
        _currentCardPosition.value = position
    }

    fun decreaseCurrentCardPosition() {
        val currentPosition = _currentCardPosition.value ?: 0
        if (currentPosition != 0) {
            _currentCardPosition.value = currentPosition - 1
        }
    }

    fun resetCurrentCardPosition() {
        _currentCardPosition.value = 0
    }

    fun addSwipedItem(item: MediaItem) {
        val currentList = _itemsToDelete.value ?: listOf()
        if (!currentList.contains(item)) {
            _itemsToDelete.value = currentList + item
        } else {
            Log.d("MainViewModel", "Item already in the swipedList")
        }
    }

    fun removeSwipedItem(item: MediaItem) {
        val currentList = _itemsToDelete.value ?: listOf()
        val updatedList = currentList.filter { it != item }
        _itemsToDelete.value = updatedList
    }

    fun removeAllSwipedItems() {
        _itemsToDelete.value = emptyList()
    }

    fun givePermissionRead() {
        _permissionStateRead.value = true
    }

    fun takePermissionRead() {
        _permissionStateRead.value = false
    }

    fun setPermissionState(state: Boolean) {
        _permissionStateRead.value = state
    }



    fun getMediaItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val allMediaItems = mutableListOf<MediaItem>()
            val photos = mutableListOf<MediaItem>()
            val videos = mutableListOf<MediaItem>()
            val screenshots = mutableListOf<MediaItem>()

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
                            relativePath.contains("Movies")
                        ) {
                            if (!name.startsWith(".")) {
                                val mediaItem = MediaItem(
                                    id = id,
                                    uri = contentUri,
                                    name = name,
                                    size = size,
                                    dateAdded = dateAdded,
                                    isVideo = mimeType.startsWith("video"),
                                    relativePath = relativePath
                                )
                                allMediaItems.add(mediaItem)

                                if (mimeType.startsWith("image")) {
                                    if (!isScreenshot(name)) {
                                        photos.add(mediaItem)
                                    } else {
                                        screenshots.add(mediaItem)
                                    }
                                } else if (mimeType.startsWith("video")) {
                                    videos.add(mediaItem)
                                }
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _allMediaItems.value = allMediaItems
                _allPhotos.value = photos
                _allVideos.value = videos
                _allScreenshots.value = screenshots
                updateGroupedMediaItems(_groupedType.value ?: "AllMedia")
            }
        }
    }

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
                _deletionComplete.value = true
                updateGroupedMediaItems(groupType)
            }
        }
    }

    fun getMediaItemsByAlbum(mediaItems: List<MediaItem>): List<AlbumGroup> {
        Log.d("MainViewModel", "getMediaItemsByAlbum called with ${mediaItems.size} items")

        val groupedList = mediaItems.groupBy { item ->
            val path = item.relativePath
            Log.d("MainViewModel", "Processing item with path: $path")
            when {
                path.startsWith("DCIM/") -> {
                    val folderName = path.removePrefix("DCIM/").split("/").firstOrNull()
                    if (folderName.isNullOrEmpty() || folderName.startsWith(".")) {
                        "DCIM"
                    } else {
                        "$folderName"
                    }
                }
                path.startsWith("Pictures/") -> {
                    val folderName = path.removePrefix("Pictures/").split("/").firstOrNull()
                    if (folderName.isNullOrEmpty() || folderName.startsWith(".")) {
                        "Pictures"
                    } else {
                        "$folderName"
                    }
                }
                path.startsWith("Movies/") -> {
                    val folderName = path.removePrefix("Movies/").split("/").firstOrNull()
                    if (folderName.isNullOrEmpty() || folderName.startsWith(".")) {
                        "Movies"
                    } else {
                        "$folderName"
                    }
                }
                path.startsWith("Download/") -> "Downloads"
                else -> {
                    Log.d("MainViewModel", "Unrecognized path: $path")
                    "Other"
                }
            }
        }

        val result = groupedList.map { (folder, itemsInFolder) ->
            AlbumGroup(
                name = folder,
                items = itemsInFolder.sortedByDescending { it.dateAdded }
            )
        }.sortedWith(
            compareByDescending<AlbumGroup> { it.items.size }
                .thenBy { it.name }
        )

        Log.d("MainViewModel", "getMediaItemsByAlbum finished, created ${result.size} groups")
        result.forEach { group ->
            Log.d("MainViewModel", "Group: ${group.name}, Items: ${group.items.size}")
        }
        return result
    }

    fun updateGroupedMediaItems(type: String) {
        viewModelScope.launch {
            val sourceList = when (type) {
                "AllMedia" -> _allMediaItems.value
                "AllPhotos" -> _allPhotos.value
                "AllVideos" -> _allVideos.value
                "AllScreenshots" -> _allScreenshots.value
                else -> null
            }

            Log.d("MainViewModel", "Updating grouped media items for type: $type")
            Log.d("MainViewModel", "Source list size: ${sourceList?.size ?: 0}")

            val grouped = getMediaItemsByAlbum(sourceList ?: emptyList())
            _groupedMediaItems.postValue(grouped)

            Log.d("MainViewModel", "Updated grouped media items: ${grouped.size} groups")
            grouped.forEach { group ->
                Log.d("MainViewModel", "Group: ${group.name}, Items: ${group.items.size}")
            }
        }
    }


    private fun isScreenshot(fileName: String): Boolean {
        return fileName.lowercase().contains("screenshot")
    }

    fun bytesToGB(bytes: Long): Double {
        return bytes / (1024.0 * 1024.0 * 1024.0)
    }
}
