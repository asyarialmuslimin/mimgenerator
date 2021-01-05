package com.saifur.mimgenerator.ui.mainactivity

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saifur.mimgenerator.R
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.databinding.MemeRowBinding
import com.saifur.mimgenerator.ui.showmeme.ShowMemeActivity

class MemeAdapter : RecyclerView.Adapter<MemeAdapter.ViewHolder>() {

    private val memeList = ArrayList<Meme>()

    fun setData(items:List<Meme>){
        memeList.clear()
        memeList.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding:MemeRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item:Meme){
            with(binding){
                Glide.with(root.context)
                    .load(item.url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(memeImage)

                memeImage.setOnClickListener {
                    val intent = Intent(root.context, ShowMemeActivity::class.java)
                    intent.putExtra(ShowMemeActivity.EXTRA_DETAIL, item)
                    root.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MemeRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(memeList[position])
    }

    override fun getItemCount(): Int = memeList.size
}