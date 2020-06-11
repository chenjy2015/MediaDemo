package com.example.mediademo.ui.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class KTBaseUIActivity<VD : ViewDataBinding> : AppCompatActivity() {

    lateinit var dataBinding: VD

    abstract fun getLayoutId(): Int
    abstract fun init()
    abstract fun initEvent()
    abstract fun initData()
    fun preCreate() {}


    override fun onCreate(savedInstanceState: Bundle?) {
        preCreate()
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,getLayoutId())
        init()
        initEvent()
        initData()
    }
}