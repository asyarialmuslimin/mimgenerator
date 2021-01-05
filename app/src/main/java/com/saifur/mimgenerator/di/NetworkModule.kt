package com.saifur.mimgenerator.di

import com.saifur.mimgenerator.data.repository.MemeService
import com.saifur.mimgenerator.utils.ResponseHandler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val BASE_URL = "https://api.imgflip.com/"

val networkModule = module {
    factory { provideOkHttpLogging() }
    factory { provideOkHttpClient(get()) }
    factory { provideMemeApi(get()) }
    single { provideRetrofit(get()) }
    factory { ResponseHandler() }
}

fun provideRetrofit(okHttpClient:OkHttpClient) : Retrofit{
    return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build()
}

fun provideOkHttpLogging(): HttpLoggingInterceptor {
    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    return logging
}

fun provideOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient {

    return OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}

fun provideMemeApi(retrofit:Retrofit) : MemeService = retrofit.create(MemeService::class.java)