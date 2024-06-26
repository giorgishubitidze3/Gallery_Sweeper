package com.example.gallerysweeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gallerysweeper.R
import com.example.gallerysweeper.adapters.CardViewAdapter
import com.example.gallerysweeper.databinding.FragmentCardStackBinding

class CardStackFragment : Fragment() {

    private lateinit var binding : FragmentCardStackBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCardStackBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardStackView = binding.cardViewFragment

        val adapter = CardViewAdapter()

        cardStackView.adapter = adapter



    }




}