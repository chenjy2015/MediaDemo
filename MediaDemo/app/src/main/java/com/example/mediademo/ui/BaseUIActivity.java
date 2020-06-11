package com.example.mediademo.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseUIActivity<VD extends ViewDataBinding> extends AppCompatActivity {

    private VD dataBinding;

    public abstract int getLayoutId();

    public void preCreate() {

    }

    public abstract void init();

    public abstract void initEvent();

    public abstract void initData();

    public VD getDataBinding() {
        return dataBinding;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preCreate();
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        init();
        initEvent();
        initData();
    }
}
