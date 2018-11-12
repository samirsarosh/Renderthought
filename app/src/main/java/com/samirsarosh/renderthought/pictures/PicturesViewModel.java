package com.samirsarosh.renderthought.pictures;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.samirsarosh.renderthought.data.Picture;
import com.samirsarosh.renderthought.data.PictureRepository;

import java.util.List;

/**
 * Created by samirsarosh on 11/13/18.
 */

public class PicturesViewModel extends ViewModel {

    private LiveData<List<Picture>> picturesList;

    public void init(PictureRepository pictureRepository) {
        this.picturesList = pictureRepository.getPictures();
    }

    public LiveData<List<Picture>> getAllPictures() {
        return this.picturesList;
    }
}
