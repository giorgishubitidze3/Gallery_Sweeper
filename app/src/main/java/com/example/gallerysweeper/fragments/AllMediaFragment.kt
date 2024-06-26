package com.example.gallerysweeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.AllMediaAdapter
import com.example.gallerysweeper.databinding.FragmentAllMediaBinding


class AllMediaFragment : Fragment() {

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

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val recyclerView = binding.recyclerViewAllItems
        val adapter = AllMediaAdapter()

        viewModel.groupedMediaItems.observe(viewLifecycleOwner){list ->
            adapter.setData(list)
        }

        recyclerView.adapter = adapter


    }


}