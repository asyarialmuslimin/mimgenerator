package com.saifur.mimgenerator.data.repository

import com.saifur.mimgenerator.data.model.memeresponse.MemeResponse
import retrofit2.http.GET

interface MemeService {
    @GET("get_memes")
    suspend fun getMemeList() : MemeResponse
}