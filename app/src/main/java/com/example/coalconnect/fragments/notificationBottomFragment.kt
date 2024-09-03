package com.example.coalconnect.fragments

import com.example.coalconnect.databinding.FragmentNotificationBottomBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coalconnect.R
import com.example.coalconnect.adapters.NotificationAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class notification_bottom_fragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationBottomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBottomBinding.inflate(layoutInflater, container, false)

        val notifications = listOf(
            "Kaam Toh Karna padega"
        )

        val notificationImages = listOf(R.drawable.sademoji)
        val adapter = NotificationAdapter(ArrayList(notifications), ArrayList( notificationImages))

        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = adapter

        return binding.root
    }

    companion object {

    }
}