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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.CardViewAdapter
import com.example.gallerysweeper.data.AlbumGroup
import com.example.gallerysweeper.databinding.FragmentCardStackBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.RewindAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod

class CardStackFragment : Fragment() {

    private lateinit var binding: FragmentCardStackBinding
    lateinit var layoutManager: CardStackLayoutManager
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: CardViewAdapter
    private lateinit var cardStackView: CardStackView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCardStackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val navController = activity?.findNavController(R.id.fragment_container)

        val selectedFiles: AlbumGroup? = arguments?.getParcelable("selectedAlbum")
        selectedFiles?.let {
            Toast.makeText(requireContext(), "Size of the data: ${it.items.size}", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "Selected files are null", Toast.LENGTH_SHORT).show()
        }
        adapter = CardViewAdapter(requireContext())
        selectedFiles?.items?.let { adapter.setData(it) }

        cardStackView = binding.cardViewFragment

        layoutManager = CardStackLayoutManager(requireContext(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {
                Log.d("CardStackListenerImpl", "onCardDragging Dragging $direction direction and ratio: $ratio")
                val topView = layoutManager.topView ?: return

                val rightOverlay: View? = topView.findViewById(R.id.left_overlay)
                val leftOverlay: View? = topView.findViewById(R.id.right_overlay)

                when (direction) {
                    Direction.Left -> {
                        leftOverlay?.visibility = View.VISIBLE
                        rightOverlay?.visibility = View.GONE
                        leftOverlay?.alpha = ratio
                    }
                    Direction.Right -> {
                        rightOverlay?.visibility = View.VISIBLE
                        leftOverlay?.visibility = View.GONE
                        rightOverlay?.alpha = ratio
                    }
                    else -> {
                        leftOverlay?.visibility = View.GONE
                        rightOverlay?.visibility = View.GONE
                    }
                }
            }

            override fun onCardSwiped(direction: Direction?) {
                if (direction == Direction.Right) {
                    val swipedPosition = layoutManager.topPosition - 1
                    val swipedItem = adapter.getItem(swipedPosition)
                    viewModel.addSwipedItem(swipedItem)
                    Log.d("CardStackListenerImpl", "onCardSwiped direction: $direction")
                } else if (direction == Direction.Left) {
                    val swipedPosition = layoutManager.topPosition - 1
                    val swipedItem = adapter.getItem(swipedPosition)
                    viewModel.removeSwipedItem(swipedItem)
                }

                val topView = layoutManager.topView ?: return
                val leftOverlay: View = topView.findViewById(R.id.left_overlay)
                val rightOverlay: View = topView.findViewById(R.id.right_overlay)

                leftOverlay.visibility = View.GONE
                rightOverlay.visibility = View.GONE
            }

            override fun onCardRewound() {
                Log.d("CardStackListenerImpl", "onCardRewound")
            }

            override fun onCardCanceled() {
                Log.d("CardStackListenerImpl", "onCardCanceled")
            }

            override fun onCardAppeared(view: View?, position: Int) {
                Log.d("CardStackListenerImpl", "onCardAppeared at position $position")
                updateVisibleVideos()
            }

            override fun onCardDisappeared(view: View?, position: Int) {
                Log.d("CardStackListenerImpl", "onCardDisappeared at position $position")
                viewModel.setCurrentCardPosition(position + 1)
                updateVisibleVideos()
            }
        }).apply {
            setDirections(Direction.HORIZONTAL)
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setCanScrollVertical(false)
        }

        viewModel.currentCardPosition.observe(viewLifecycleOwner) { position ->
            layoutManager.scrollToPosition(position)
        }

        cardStackView.adapter = adapter
        cardStackView.layoutManager = layoutManager

        cardStackView.post{
            initializeFirstVideo()
        }

        viewModel.itemsToDelete.observe(viewLifecycleOwner) { list ->
            Log.d("CardStackListenerImpl", "${list.size}")
        }

        binding.btnCancel.setOnClickListener {
            viewModel.removeAllSwipedItems()
            navController?.navigate(R.id.action_cardStackFragment_to_allMediaFragment)
        }

        binding.btnReset.setOnClickListener {
            Toast.makeText(requireContext(), "btn clicked", Toast.LENGTH_SHORT).show()
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .build()
            layoutManager.setRewindAnimationSetting(setting)
            cardStackView.rewind()
            viewModel.decreaseCurrentCardPosition()
        }

        binding.btnDone.setOnClickListener {
            navController?.navigate(R.id.action_cardStackFragment_to_listToDeleteFragment)
        }
    }

    private fun updateVisibleVideos() {
        val topPosition = layoutManager.topPosition

        val holder = cardStackView.findViewHolderForAdapterPosition(topPosition)
        if (holder != null) {
            adapter.initializeVideoIfNeeded(holder, topPosition)
        }

        for (i in 0 until adapter.itemCount) {
            if (i != topPosition) {
                val otherHolder = cardStackView.findViewHolderForAdapterPosition(i)
                if (otherHolder != null) {
                    adapter.releaseVideoIfNeeded(otherHolder)
                }
            }
        }
    }

    private fun initializeFirstVideo(position : Int = 0) {
        if (adapter.itemCount > 0) {
            val firstHolder = cardStackView.findViewHolderForAdapterPosition(0)
            if (firstHolder != null) {
                adapter.initializeVideoIfNeeded(firstHolder, 0)
            }else {

                cardStackView.post {
                    initializeFirstVideo(position)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()



        val selectedFiles = arguments?.getParcelable<AlbumGroup>("selectedAlbum")
        selectedFiles?.let { AlbumGroup ->
            val updatedItems = AlbumGroup.items.filter { item ->
                viewModel.allMediaItems.value?.contains(item) ?: false
            }

            if (updatedItems.size != AlbumGroup.items.size) {
                adapter.setData(updatedItems)
                adapter.notifyDataSetChanged()

                if (layoutManager.topPosition >= updatedItems.size) {
                    layoutManager.scrollToPosition(0)
                    viewModel.resetCurrentCardPosition()
                }
                else{
                    viewModel.currentCardPosition.value?.let { layoutManager.scrollToPosition(it) }
                }
                val updatedMonthGroup = AlbumGroup(AlbumGroup.name, updatedItems)
                arguments?.putParcelable("selectedMonth", updatedMonthGroup)

                Toast.makeText(
                    requireContext(),
                    "Updated. New size: ${updatedItems.size}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.currentCardPosition.value?.let { position ->
            cardStackView.post {
                initializeFirstVideo(position)
            }
        }
        updateVisibleVideos()


    }

    override fun onPause() {
        super.onPause()
        for (i in 0 until adapter.itemCount) {
            val holder = cardStackView.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                adapter.releaseVideoIfNeeded(holder)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetCurrentCardPosition()
    }
}