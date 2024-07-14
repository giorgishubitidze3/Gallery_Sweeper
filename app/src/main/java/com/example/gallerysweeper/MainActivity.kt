package com.example.gallerysweeper

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var sharedPrefs: SharedPreferences
    private val permissionGrantedKey = "media_permissions_granted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        actionBar?.hide()




        sharedPrefs = getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                sharedPrefs.edit().putBoolean(permissionGrantedKey, true).apply()
                viewModel.givePermissionRead()
                Log.d("MainActivityDebug", "Permissions granted")
                checkManageExternalStoragePermission() // Add this line
            } else {
                Log.d("MainActivityDebug", "Permissions not granted")
                viewModel.takePermissionRead()
                showPermissionExplanationDialog()
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                   READ_MEDIA_IMAGES,
                   READ_MEDIA_VIDEO
                )
            }
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
//                arrayOf(READ_EXTERNAL_STORAGE)
//            }
            else -> {
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                )
            }
        }

        val allPermissionsGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            viewModel.givePermissionRead()
            Log.d("MainActivityDebug", "All permissions already granted")
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
            Log.d("MainActivityDebug", "Requesting permissions")
        }
    }

    private fun showPermissionExplanationDialog() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                viewModel.getMediaItems(this)
            }
        } else {
            viewModel.getMediaItems(this)
        }
    }
}

