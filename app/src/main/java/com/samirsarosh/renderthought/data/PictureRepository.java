package com.samirsarosh.renderthought.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.samirsarosh.renderthought.data.local.PictureDao;
import com.samirsarosh.renderthought.data.remote.ApiConstants;
import com.samirsarosh.renderthought.data.remote.Webservice;

import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by samirsarosh on 11/11/18.
 */

public class PictureRepository {
    private Webservice webservice;
    private PictureDao pictureDao;
    private Executor executor;

    public PictureRepository() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        this.webservice = retrofit.create(Webservice.class);
//        this.pictureDao = pictureDao;
//        this.executor = executor;
    }

    public LiveData<List<Picture>> getPictures() {
        final MutableLiveData<List<Picture>> data = new MutableLiveData<>();

        webservice.getPictures().enqueue(new Callback<List<Picture>>() {
            @Override
            public void onResponse(Call<List<Picture>> call, Response<List<Picture>> response) {
                data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Picture>> call, Throwable t) {

            }
        });

        return data;
    }
}
