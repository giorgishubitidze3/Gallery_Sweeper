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
import com.example.gallerysweeper.data.MediaItem
import com.example.gallerysweeper.databinding.FragmentListToDeleteBinding

class ListToDeleteFragment : Fragment() {

    private lateinit var binding : FragmentListToDeleteBinding
    private lateinit var viewModel : MainViewModel
    private var checkedItems = emptySet<MediaItem>()


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

        val adapter = DeleteAdapter(requireContext())

        viewModel.itemsToDelete.observe(viewLifecycleOwner){list ->
            adapter.setData(list)
        }

        val gridLayout = GridLayoutManager(requireContext(),3)
        binding.rvListToDelete.layoutManager = gridLayout
        binding.rvListToDelete.adapter=adapter

        adapter.getCheckedItems().observe(viewLifecycleOwner) { checkedItems ->
            updateRestoreButtonVisibility(checkedItems)
            viewModel.setCheckedItemToDelete(checkedItems)
            this.checkedItems = checkedItems
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
                if (checkedItems.isEmpty()) {
                    viewModel.deleteMediaItems(requireContext(), it1, false)
                } else {
                    viewModel.deleteMediaItems(requireContext(), it1, true)
                }
            }
        }
        viewModel.deletionComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                // Clear checked items in the adapter
                adapter.clearCheckedItems()
                updateRestoreButtonVisibility(mutableSetOf<MediaItem>())

                // Reset the deletion complete state
                viewModel.setDeletionCompleteValue(false)
            }
        }


    }



    private fun updateRestoreButtonVisibility(checkedItems: MutableSet<com.example.gallerysweeper.data.MediaItem>?) {
        binding.btnRestore.visibility = if (checkedItems.isNullOrEmpty()) View.GONE else View.VISIBLE
        if (!checkedItems.isNullOrEmpty()){
            binding.btnClean.text = "Clean [${checkedItems.size}]"
        }else{
            binding.btnClean.text = "Clean"
        }
    }


}