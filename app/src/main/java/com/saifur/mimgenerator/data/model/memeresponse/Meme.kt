package com.saifur.mimgenerator.data.model.memeresponse

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Meme(
    val box_count: Int,
    val height: Int,
    val id: String,
    val name: String,
    val url: String,
    val width: Int
) : Parcelable