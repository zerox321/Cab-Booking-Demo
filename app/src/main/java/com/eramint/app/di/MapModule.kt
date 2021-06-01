package com.eramint.app.di

import android.content.Context
import androidx.core.content.ContextCompat
import com.eramint.app.R
import com.eramint.app.util.mapUtility.*
import com.google.android.gms.maps.model.PolylineOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    @Singleton
    @Provides
    fun provideSpherical(): Spherical = Spherical()



    @Singleton
    @Provides
    fun provideRouteEvaluator(): RouteEvaluator = RouteEvaluator()

    @Singleton
    @Provides
    fun provideMarkerAnimation(): MarkerAnimation = MarkerAnimation()

    @Singleton
    @Provides
    fun provideMapUtility(): MapUtility = MapUtility()

    @Singleton
    @Provides
    fun provideDirectionRepo(
        @ApplicationContext context: Context
    ): DirectionRepo =
        DirectionRepo(key = context.getString(R.string.google_maps_key))

    @Singleton
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
