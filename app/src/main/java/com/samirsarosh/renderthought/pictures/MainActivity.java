package com.samirsarosh.renderthought.pictures;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.databinding.library.baseAdapters.BR;
import com.samirsarosh.renderthought.R;
import com.samirsarosh.renderthought.data.Picture;
import com.samirsarosh.renderthought.data.PictureRepository;
import com.samirsarosh.renderthought.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewDataBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

        PicturesViewModel viewModel = ViewModelProviders.of(this).get(PicturesViewModel.class);
        viewModel.init(new PictureRepository());

        viewModel.getAllPictures().observe(this, new Observer<List<Picture>>() {
            @Override
            public void onChanged(@Nullable List<Picture> pictures) {
                Picture picture = pictures.get(0);
                activityMainBinding.setVariable(BR.picture, picture);
            }
        });
    }
}
