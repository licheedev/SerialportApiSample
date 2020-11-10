package com.licheedev.serialportapisample

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding

abstract class BaseBindingActivity<T : ViewDataBinding> : AppCompatActivity() {


    private var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflate().invoke(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    
    /**
     * return XxxBinding::inflate
     */
    abstract fun bindingInflate(): (LayoutInflater) -> T

}