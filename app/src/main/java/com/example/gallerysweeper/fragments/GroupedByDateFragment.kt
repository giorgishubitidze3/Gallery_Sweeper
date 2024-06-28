package com.example.gallerysweeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.AllMediaAdapter
import com.example.gallerysweeper.databinding.FragmentAllMediaBinding


class GroupedByDateFragment : Fragment() {

    private lateinit var binding: FragmentAllMediaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAllMediaBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = activity?.findNavController(R.id.fragment_container)

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val recyclerView = binding.recyclerViewAllItems
        val adapter = navController?.let { AllMediaAdapter(it) }

        viewModel.groupedMediaItems.observe(viewLifecycleOwner){list ->
            adapter?.setData(list)
        }

        recyclerView.adapter = adapter



    }


}