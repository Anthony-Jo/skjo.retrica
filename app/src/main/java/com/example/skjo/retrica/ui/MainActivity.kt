package com.example.skjo.retrica.ui

import androidx.activity.viewModels
import com.example.skjo.retrica.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {
        viewModel.data.observe(this) {
            binding.tvFps.text = it
        }

        viewModel.fetchData()
    }
}
