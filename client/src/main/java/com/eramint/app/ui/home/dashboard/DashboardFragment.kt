package com.eramint.app.ui.home.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.eramint.app.databinding.FragmentDashboardBinding
import timber.log.Timber


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        root.setOnTouchListener { v, event -> //show dialog here
            Timber.e("touck ${event.action}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Toast.makeText(
                        v.context,
                        "ACTION_DOWN ",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnTouchListener true

                }

                MotionEvent.ACTION_UP -> {
                    Toast.makeText(
                        v.context,
                        "ACTION_UP ",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnTouchListener true

                }
                else -> return@setOnTouchListener false
            }

        }
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}