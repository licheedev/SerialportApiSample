package com.licheedev.serialportapisample

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.licheedev.hwutils.ByteUtil
import com.licheedev.serialportapisample.databinding.ActivityMainBinding
import com.licheedev.serialportapisample.serial.SerialManager
import com.licheedev.serialportapisample.serial.SerialWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    private val viewModel by viewModels<MainViewModel>()


    override fun bindingInflate(): (LayoutInflater) -> ActivityMainBinding {
        return ActivityMainBinding::inflate
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.clickProxy = ClickProxy()
    }

    private fun Any?.showToast() {
        Toast.makeText(this@MainActivity, this.toString(), Toast.LENGTH_SHORT).show()
    }

    inner class ClickProxy {

        private val serialWorker: SerialWorker get() = SerialManager.get().serialWorker

        fun openDevice() {

            viewModel.viewModelScope.launch {

                val success = withContext(Dispatchers.IO) {
                    try {
                        serialWorker.open(File("/dev/ttyS4"), 115200)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }

                if (success) {
                    "open device success!".showToast()
                } else {
                    "open device failure!".showToast()
                }

            }

        }

        fun checkBattery() {

            viewModel.viewModelScope.launch {
                
                val recv = withContext(Dispatchers.IO) {
                    for (i in 1..10) {
                        val recvBytes = serialWorker.sendNoThrow(ByteUtil.hexStr2bytes("40BF"))
                        if (recvBytes != null) {
                            return@withContext recvBytes
                        }
                    }
                    return@withContext null
                }

                if (recv != null) {
                    "check battery success!".showToast()
                    // TODO: handle recv
                } else {
                    "check battery failure!".showToast()
                }
            }

        }

    }


}