package com.saifur.mimgenerator.di

import com.saifur.mimgenerator.ui.mainactivity.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel(get()) }
}