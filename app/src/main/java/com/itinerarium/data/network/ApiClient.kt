package com.itinerarium.data.network

import com.itinerarium.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP / Singleton Retrofit configurado para a API OpenRouteService (ORS) e Nominatim (OSM).
 */
object ApiClient {

    private const val ORS_BASE_URL = "https://api.openrouteservice.org/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        builder.build()
    }

    private val orsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ORS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val nominatimRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val routeApiService: RouteApiService by lazy {
        orsRetrofit.create(RouteApiService::class.java)
    }

    val nominatimApiService: NominatimApiService by lazy {
        nominatimRetrofit.create(NominatimApiService::class.java)
    }
}
