package org.sheedon.requestrepository;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;

import org.sheedon.requestrepository.viewmodel.AnnotationViewModel;
import org.sheedon.requestrepository.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private AnnotationViewModel anViewModel;
    private ViewDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setVariable(BR.vm, mainViewModel);

        anViewModel = new ViewModelProvider(this).get(AnnotationViewModel.class);
        binding.setVariable(BR.anVm, anViewModel);
        anViewModel.initConfig();
    }

    public void onLoginClick(View view) {
        mainViewModel.loginClick();
    }

    public void onAnLoginClick(View view) {
        anViewModel.login(mainViewModel.account, mainViewModel.password);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }
}