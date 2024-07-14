package com.example.gallerysweeper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.CardViewAdapter
import com.example.gallerysweeper.data.AlbumGroup
import com.example.gallerysweeper.databinding.FragmentCardStackBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.RewindAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeableMethod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.core.util.Pair as UtilPair


class CardStackFragment : Fragment() {

    private lateinit var binding: FragmentCardStackBinding
    lateinit var layoutManager: CardStackLayoutManager
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: CardViewAdapter
    private lateinit var cardStackView: CardStackView


    private var startDate: Long = 0L
    private var endDate: Long = 0L

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

        binding.btnCalendar.setOnClickListener {
            showDateRangePicker()
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
        selectedFiles?.let { albumGroup ->
            val updatedItems = if (startDate != 0L && endDate != 0L) {
                albumGroup.items.filter { item ->
                    item.dateAdded in startDate..endDate &&
                            (viewModel.allMediaItems.value?.contains(item) ?: false)
                }
            } else {
                albumGroup.items.filter { item ->
                    viewModel.allMediaItems.value?.contains(item) ?: false
                }
            }

            if (updatedItems.isEmpty() && (startDate != 0L || endDate != 0L)) {
                // Reset the filter if the filtered list is empty
                startDate = 0L
                endDate = 0L
                adapter.setData(albumGroup.items)
                Toast.makeText(requireContext(), "Filter reset. Showing all items.", Toast.LENGTH_SHORT).show()
            } else if (updatedItems.size != albumGroup.items.size) {
                adapter.setData(updatedItems)
            }

            adapter.notifyDataSetChanged()

            if (layoutManager.topPosition >= updatedItems.size) {
                layoutManager.scrollToPosition(0)
                viewModel.resetCurrentCardPosition()
            } else {
                viewModel.currentCardPosition.value?.let { layoutManager.scrollToPosition(it) }
            }

            val updatedAlbumGroup = AlbumGroup(albumGroup.name, updatedItems)
            arguments?.putParcelable("selectedAlbum", updatedAlbumGroup)

            Log.d("OnResumeDebug", "Updated items: ${updatedItems.size}")
            Log.d("OnResumeDebug", "Original items: ${albumGroup.items.size}")
        }

        viewModel.currentCardPosition.value?.let { position ->
            cardStackView.post {
                initializeFirstVideo(position)
            }
        }

        updateVisibleVideos()
    }

    private fun showDateRangePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getDefault())

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date range")
            .setSelection(
                UtilPair(
                    today,
                    today
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            Log.d("DatePickerDebug", "Raw start: ${Date(selection.first ?: today)}")
            Log.d("DatePickerDebug", "Raw end: ${Date(selection.second ?: today)}")

            // Set start date to the beginning of the selected day in device's time zone
            calendar.timeInMillis = selection.first ?: today
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            startDate = calendar.timeInMillis

            // Set end date to the end of the selected day in device's time zone
            calendar.timeInMillis = selection.second ?: today
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            endDate = calendar.timeInMillis

            Log.d("DatePickerDebug", "Adjusted start: ${Date(startDate)}")
            Log.d("DatePickerDebug", "Adjusted end: ${Date(endDate)}")

            filterMediaItems()
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun filterMediaItems() {
        val selectedFiles = arguments?.getParcelable<AlbumGroup>("selectedAlbum")
        selectedFiles?.let { albumGroup ->
            val filteredItems = albumGroup.items.filter { item ->
                val itemDate = Calendar.getInstance().apply {
                    timeInMillis = item.dateAdded
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                itemDate in startDate..endDate
            }
            adapter.setData(filteredItems)
            adapter.notifyDataSetChanged()
            layoutManager.scrollToPosition(0)
            viewModel.resetCurrentCardPosition()

            // Debug information
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault())
            Log.d("FilterDebug", "Start Date: ${dateFormat.format(Date(startDate))}")
            Log.d("FilterDebug", "End Date: ${dateFormat.format(Date(endDate))}")
            Log.d("FilterDebug", "Filtered items: ${filteredItems.size}")
            filteredItems.forEach {
                Log.d("FilterDebug", "Item date: ${dateFormat.format(Date(it.dateAdded))}")
            }

            // Log all items for debugging
            Log.d("FilterDebug", "All items: ${albumGroup.items.size}")
            albumGroup.items.forEach {
                Log.d("FilterDebug", "All item date: ${dateFormat.format(Date(it.dateAdded))}")
            }

            Toast.makeText(requireContext(), "Filtered items: ${filteredItems.size}", Toast.LENGTH_SHORT).show()
        }
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