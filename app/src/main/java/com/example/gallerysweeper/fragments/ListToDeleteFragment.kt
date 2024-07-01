package com.example.gallerysweeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.adapters.DeleteAdapter
import com.example.gallerysweeper.databinding.FragmentListToDeleteBinding

class ListToDeleteFragment : Fragment() {

    private lateinit var binding : FragmentListToDeleteBinding
    private lateinit var viewModel : MainViewModel



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


         viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val adapter = DeleteAdapter()

        viewModel.itemsToDelete.observe(viewLifecycleOwner){list ->
            adapter.setData(list)
        }

        val gridLayout = GridLayoutManager(requireContext(),3)
        binding.rvListToDelete.layoutManager = gridLayout
        binding.rvListToDelete.adapter=adapter

        adapter.getCheckedItems().observe(viewLifecycleOwner) { checkedItems ->
            updateRestoreButtonVisibility(checkedItems)
        }

        binding.btnRestore.setOnClickListener {
            adapter.getCheckedItems().value?.forEach {
                viewModel.removeSwipedItem(it)
            }
            adapter.clearCheckedItems()
            updateRestoreButtonVisibility(adapter.getCheckedItems().value)
        }

        binding.btnClean.setOnClickListener {
            viewModel.groupedType.value?.let { it1 ->
                viewModel.deleteMediaItems(requireContext(),
                    it1
                )
            }
        }


    }

    private fun updateRestoreButtonVisibility(checkedItems: MutableSet<com.example.gallerysweeper.data.MediaItem>?) {
        binding.btnRestore.visibility = if (checkedItems.isNullOrEmpty()) View.GONE else View.VISIBLE
    }
}