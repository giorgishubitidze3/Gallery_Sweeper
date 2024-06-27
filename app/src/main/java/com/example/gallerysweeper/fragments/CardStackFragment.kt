package com.example.gallerysweeper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.CardViewAdapter
import com.example.gallerysweeper.data.MonthGroup
import com.example.gallerysweeper.databinding.FragmentCardStackBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.RewindAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod

class CardStackFragment : Fragment() {

    private lateinit var binding : FragmentCardStackBinding
    lateinit var layoutManager: CardStackLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCardStackBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val navController = activity?.findNavController(R.id.fragment_container)


        val selectedFiles: MonthGroup? = arguments?.getParcelable("selectedMonth")
        selectedFiles?.let {
            Toast.makeText(requireContext(), "Size of the data: ${it.items.size}", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "Selected files are null", Toast.LENGTH_SHORT).show()
        }
        val adapter = CardViewAdapter()
        selectedFiles?.items?.let { adapter.setData(it) }

        val cardStackView = binding.cardViewFragment

        layoutManager = CardStackLayoutManager(requireContext(), object:CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d("CardStackListenerImpl", "onCardDragging Dragging $direction direction and ratio: $ratio")
            }

            override fun onCardSwiped(direction: Direction?) {
                if(direction == Direction.Left){
                    val swipedPosition = layoutManager.topPosition - 1
                    val swipedItem = adapter.getItem(swipedPosition)
                    viewModel.addSwipedItem(swipedItem)
                    Log.d("CardStackListenerImpl", "onCardSwiped direction: $direction")
                }else{
                    Log.d("CardStackListenerImpl", "onCardSwiped direction: $direction")
                }
            }

            override fun onCardRewound() {
                Log.d("CardStackListenerImpl", "onCardRewound")
            }

            override fun onCardCanceled() {
                Log.d("CardStackListenerImpl", "onCardCanceled")
            }

            override fun onCardAppeared(view: View?, position: Int) {
                Log.d("CardStackListenerImpl", "onCardAppeared")
            }

            override fun onCardDisappeared(view: View?, position: Int) {
                Log.d("CardStackListenerImpl", "onCardDisappeared")
            }

        }).apply{
            setDirections(Direction.HORIZONTAL)
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setCanScrollVertical(false)
        }

        cardStackView.adapter = adapter
        cardStackView.layoutManager = layoutManager


        viewModel.itemsToDelete.observe(viewLifecycleOwner){ list ->
            Log.d("CardStackListenerImpl","${list.size}")
        }


        binding.btnReset.setOnClickListener {
            Toast.makeText(requireContext(),"btn clicked",Toast.LENGTH_SHORT).show()
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .build()
            layoutManager.setRewindAnimationSetting(setting)
            cardStackView.rewind()
        }

        binding.btnDone.setOnClickListener{
            navController?.navigate(R.id.action_cardStackFragment_to_listToDeleteFragment)
        }
    }

}