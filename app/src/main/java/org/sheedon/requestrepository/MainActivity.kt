package org.sheedon.requestrepository

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.sheedon.requestrepository.viewmodel.MainViewModel
import org.sheedon.requestrepository.viewmodel.AnnotationViewModel
import androidx.databinding.ViewDataBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private var mainViewModel: MainViewModel? = null
    private var anViewModel: AnnotationViewModel? = null
    private lateinit var binding: ViewDataBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.setVariable(BR.vm, mainViewModel)
        anViewModel = ViewModelProvider(this).get(AnnotationViewModel::class.java)
        binding.setVariable(BR.anVm, anViewModel)
        anViewModel!!.initConfig()
    }

    fun onLoginClick(view: View?) {
        mainViewModel!!.loginClick()
    }

    fun onAnLoginClick(view: View?) {
        anViewModel!!.login(mainViewModel!!.account, mainViewModel!!.password)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}