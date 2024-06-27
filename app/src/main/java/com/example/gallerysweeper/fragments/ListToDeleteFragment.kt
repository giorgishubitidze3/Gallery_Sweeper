package com.example.gallerysweeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.DeleteAdapter
import com.example.gallerysweeper.databinding.FragmentListToDeleteBinding

class ListToDeleteFragment : Fragment() {

    private lateinit var binding : FragmentListToDeleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentListToDeleteBinding.inflate(inflater,container,false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val adapter = DeleteAdapter()

        viewModel.itemsToDelete.observe(viewLifecycleOwner){list ->
            adapter.setData(list)
        }

        val gridLayout = GridLayoutManager(requireContext(),3)
        binding.rvListToDelete.layoutManager = gridLayout
        binding.rvListToDelete.adapter=adapter



    }


}