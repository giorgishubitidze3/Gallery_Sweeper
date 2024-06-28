package com.example.gallerysweeper.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val navController = activity?.findNavController(R.id.fragment_container)

        // Check permissions when the fragment starts
        checkPermissions()

        viewModel.permissionStateRead.observe(viewLifecycleOwner) { state ->
            if (state) {
                binding.cardViewWarning.visibility = View.GONE

                viewModel.getMediaItems(requireContext())

                viewModel.allMediaItems.observe(viewLifecycleOwner){list ->
                    val totalItems = list.size
                    val totalSizeBytes = list.sumOf { it.size }
                    val totalSizeGB = totalSizeBytes?.let { viewModel.bytesToGB(it) }

//                    viewModel.updateGroupedMediaItems()

                    binding.tvTotalItemCount.text = "Total items: $totalItems"
                    binding.tvTotalItemsSize.text = "%.2f GB".format(totalSizeGB)
                }

                viewModel.allPhotos.observe(viewLifecycleOwner){list ->
                    binding.tvPhotosCount.text = list.size.toString()
                    Glide.with(requireContext())
                        .load(list[0].uri)
                        .into(binding.imgPhotos)
                }

                viewModel.allVideos.observe(viewLifecycleOwner){list ->
                    binding.tvVideosCount.text = list.size.toString()

                    Glide.with(requireContext())
                        .load(list[0].uri)
                        .into(binding.imgVideos)


                }

                viewModel.allScreenshots.observe(viewLifecycleOwner){list ->
                    binding.tvScreenshotsCount.text = list.size.toString()

                    Glide.with(requireContext())
                        .load(list[0].uri)
                        .into(binding.imgScreenshots)

                }


            }



                binding.cardViewAllMedia.setOnClickListener {
                    viewModel.updateGroupedTypeValue("AllMedia")
                    navController?.navigate(R.id.action_homeFragment_to_allMediaFragment)
                    viewModel.groupedType.value?.let { it1 -> viewModel.updateGroupedMediaItems(it1) }
                }

                binding.cardViewPhotos.setOnClickListener {
                    viewModel.updateGroupedTypeValue("AllPhotos")
                    navController?.navigate(R.id.action_homeFragment_to_allMediaFragment)
                    viewModel.groupedType.value?.let { it1 -> viewModel.updateGroupedMediaItems(it1) }
                }

                binding.cardViewVideos.setOnClickListener {
                    viewModel.updateGroupedTypeValue("AllVideos")
                    navController?.navigate(R.id.action_homeFragment_to_allMediaFragment)
                    viewModel.groupedType.value?.let { it1 -> viewModel.updateGroupedMediaItems(it1) }
                }

                binding.cardViewScreenshots.setOnClickListener {
                    viewModel.updateGroupedTypeValue("AllScreenshots")
                    navController?.navigate(R.id.action_homeFragment_to_allMediaFragment)
                    viewModel.groupedType.value?.let { it1 -> viewModel.updateGroupedMediaItems(it1) }
                }

        }
    }

    private fun checkPermissions() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.setPermissionState(hasPermission)
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                viewModel.givePermissionRead()
            } else {
                Log.d("HomeFragment", "Permission denied.")
            }
        }



}

