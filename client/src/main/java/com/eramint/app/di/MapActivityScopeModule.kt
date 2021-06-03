package com.eramint.app.di

import android.content.Context
import androidx.core.content.ContextCompat
import com.eramint.app.R
import com.eramint.common.mapUtility.MapAnimator
import com.eramint.common.mapUtility.RouteEvaluator
import com.google.android.gms.maps.model.PolylineOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object MapActivityScopeModule {


    @ViewModelScoped
    @Provides
    fun providePolylineOptions(): PolylineOptions = PolylineOptions()


    @ViewModelScoped
    @Provides
    fun provideMapAnimator(
        @ApplicationContext context: Context,
        routeEvaluator: RouteEvaluator
    ): MapAnimator =
        MapAnimator(
            routeEvaluator = routeEvaluator,
            primary = ContextCompat.getColor(context, R.color.purple_700),
            second = ContextCompat.getColor(context, R.color.purple_200)
        )


}
