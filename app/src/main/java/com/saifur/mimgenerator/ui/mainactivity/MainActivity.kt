package com.saifur.mimgenerator.ui.mainactivity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.databinding.ActivityMainBinding
import com.saifur.mimgenerator.utils.Resource
import com.saifur.mimgenerator.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var memeAdapter: MemeAdapter
    private lateinit var binding : ActivityMainBinding
    private val viewModel:MainActivityViewModel by viewModel()

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        memeAdapter = MemeAdapter()
        verifyStoragePermissions(this)

        getMeme()

        binding.swipeRefresher.setOnRefreshListener {
            getMeme()
            Toast.makeText(this, "Refresh List", Toast.LENGTH_SHORT).show()
            binding.swipeRefresher.isRefreshing = false
        }

        binding.btnRetry.setOnClickListener {
            getMeme()
        }
    }

    private fun getMeme(){
        viewModel.getMeme()
        viewModel.imageList.observe(this, {
            if (it != null) {
                handleResponseStatus(it)
            }
        })
    }

    private fun fillRecyclerView(list: List<Meme>){
        memeAdapter.setData(list)
        binding.rvMeme.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = memeAdapter
        }
    }

    private fun handleResponseStatus(response: Resource<List<Meme>>){
        when(response.status){
            Status.SUCCESS -> {
                response.data?.apply {
                    fillRecyclerView(this)
                }
            }
            Status.ERROR -> {
                binding.warningMessage.text = response.message
            }
            Status.LOADING -> {
            }
        }
        handleLayoutVisibility(response.status)
    }

    private fun handleLayoutVisibility(status: Status){
        binding.progressbar.visibility = if (status == Status.LOADING) View.VISIBLE else View.GONE
        binding.errorWarning.visibility = if (status == Status.ERROR) View.VISIBLE else View.GONE
        binding.swipeRefresher.visibility = if (status == Status.SUCCESS) View.VISIBLE else View.GONE
        Log.d("Status", status.name)
    }

    private fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }
}