package org.sheedon.requestrepository;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import org.sheedon.requestrepository.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    protected ProgressDialog mLoadingDialog;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MainViewModel model = new MainViewModel();
        binding.setVm(model);
        binding.setEvent(new EventClick());

        model.result.observe(this, s -> {
            hideLoading();
            if (s != null && !s.equals(""))
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
        });
    }

    public class EventClick {
        public void onLoginClick() {
            showLoading();
            binding.getVm().loginClick();
        }
    }

    /**
     * 显示加载框
     */
    public void showLoading() {
        ProgressDialog dialog = mLoadingDialog;
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setCanceledOnTouchOutside(false);
            mLoadingDialog = dialog;
        }
        dialog.setMessage("登陆中");
        dialog.show();
    }


    /**
     * 隐藏加载框
     */
    protected void hideLoading() {
        ProgressDialog dialog = mLoadingDialog;
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }
}