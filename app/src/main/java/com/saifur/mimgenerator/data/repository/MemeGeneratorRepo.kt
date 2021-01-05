package com.saifur.mimgenerator.data.repository

import android.util.Log
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.utils.Resource
import com.saifur.mimgenerator.utils.ResponseHandler
import org.koin.dsl.module
import java.lang.Exception

val memeRepoModule = module {
    factory { MemeGeneratorRepo(get(), get()) }
}

class MemeGeneratorRepo(
    private val memeService: MemeService,
    private val responseHandler: ResponseHandler
) {

    suspend fun getMeme(callback: (Resource<List<Meme>>) -> Unit){
        try {
            val result = memeService.getMemeList().data.memes
            callback(responseHandler.handleSuccess(result))
            Log.d("Status", "Success")
        }catch (e:Exception){
            Log.d("Error", e.localizedMessage!!)
            callback(responseHandler.handleException(e))
        }
    }

}