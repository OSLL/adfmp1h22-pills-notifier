package com.example.pillnotifier.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.NotificationAdapter
import com.example.pillnotifier.model.Notification
import java.util.*


class NotificationFragment : Fragment() {
    private val notificationList: MutableList<Notification> = mutableListOf()

    init {
        val singleNotification = Notification(
            "Kimberly White didn't take Vitamin A",
            Date(2022, 1, 7)
        )
        for (i in 1..10)
            notificationList.add(singleNotification)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.rv_notification)
        recyclerView.adapter = NotificationAdapter(requireContext(), notificationList)
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            DividerItemDecoration.VERTICAL
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        return view
    }
}