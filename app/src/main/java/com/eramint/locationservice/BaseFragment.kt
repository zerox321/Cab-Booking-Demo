package com.eramint.locationservice

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.fragment.app.Fragment
import com.eramint.locationservice.ui.HomeActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseFragment : Fragment() {
    protected val TAG = javaClass.simpleName

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach")

        EventBus.getDefault().register(this)

    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "onDetach")
        EventBus.getDefault().unregister(this)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLocationEvent(locationModel: LocationModel?) { /* Do something */
        onNewLocation(locationModel = locationModel ?: return)
    }

    fun requestLocation() {
        if (activity is HomeActivity) (activity as HomeActivity).getLocation()
    }

    abstract fun onNewLocation(locationModel: LocationModel)
}