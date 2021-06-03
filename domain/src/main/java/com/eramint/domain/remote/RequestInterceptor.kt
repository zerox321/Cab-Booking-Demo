package com.eramint.domain.remote

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject


class RequestInterceptor @Inject constructor() : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val lang: String = "ar" +
                ""

        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val url = originalUrl.newBuilder().build()
        val requestBuilder = originalRequest.newBuilder().url(url).apply {

            addHeader("Accept", "application/json")
            addHeader("Content-Type", "application/json")
            addHeader("lang", lang)

        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)
        response.code//status code
        Timber.tag("RequestInterceptor").e("code ${response.code}")
        return response
    }
}