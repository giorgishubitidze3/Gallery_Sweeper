package com.example.gallerysweeper.adapters

import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.gallerysweeper.MainViewModel
import com.example.gallerysweeper.data.MediaItem
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class CardStackImpl(
    private val viewModel: MainViewModel,
    val cardStackView:CardStackView
):CardStackListener {
    override fun onCardDragging(direction: Direction?, ratio: Float) {
        Log.d("CardStackListenerImpl", "Card swiped in direction: $direction")
    }

    override fun onCardSwiped(direction: Direction?) {
        Log.d("CardStackListenerImpl", "Card swiped in direction: $direction, item: $")
    }

    override fun onCardRewound() {
        Log.d("CardStackListenerImpl", "Card swiped in direction: ")
    }

    override fun onCardCanceled() {
        Log.d("CardStackListenerImpl", "Card swiped in direction:")
    }

    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("CardStackListenerImpl", "Card swiped in direction: ")
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        Log.d("CardStackListenerImpl", "Card swiped in direction: ")
    }

}