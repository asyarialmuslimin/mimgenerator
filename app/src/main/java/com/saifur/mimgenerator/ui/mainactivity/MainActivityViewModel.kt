package com.saifur.mimgenerator.ui.mainactivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.data.repository.MemeGeneratorRepo
import com.saifur.mimgenerator.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repo: MemeGeneratorRepo) : ViewModel() {
    val imageList = MutableLiveData<Resource<List<Meme>>>()

    fun getMeme(){
        imageList.postValue(Resource.loading(null))
        CoroutineScope(Dispatchers.Main).launch {
            repo.getMeme {
                imageList.postValue(it)
            }
        }
    }
}