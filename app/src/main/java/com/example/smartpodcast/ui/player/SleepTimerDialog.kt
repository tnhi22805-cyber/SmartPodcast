package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import com.example.smartpodcast.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SleepTimerDialog(
    private val onTimerSet: (Int, Int) -> Unit,
    private val onEndOfPodcast: () -> Unit,
    private val onCancelTimer: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_sleep_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val npHour = view.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = view.findViewById<NumberPicker>(R.id.npMinute)
        val btnSet = view.findViewById<Button>(R.id.btnSetTimer)
        val btnEnd = view.findViewById<Button>(R.id.btnEndOfEpisode)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelTimer)

        // Configure Hours (0 - 12)
        npHour.minValue = 0
        npHour.maxValue = 12
        npHour.value = 0

        // Configure Minutes (0 - 59)
        npMinute.minValue = 0
        npMinute.maxValue = 59
        npMinute.value = 15

        btnSet.setOnClickListener {
            onTimerSet(npHour.value, npMinute.value)
            dismiss()
        }

        btnEnd.setOnClickListener {
            onEndOfPodcast()
            dismiss()
        }

        btnCancel.setOnClickListener {
            onCancelTimer()
            dismiss()
        }
    }
}
