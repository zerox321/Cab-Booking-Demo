package com.eramint.locationservice.base

import androidx.fragment.app.Fragment
import com.eramint.locationservice.ui.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {
    protected val TAG = javaClass.simpleName

    fun requestLocation() {
        if (activity is HomeActivity) (activity as HomeActivity).getLocation()
    }

}