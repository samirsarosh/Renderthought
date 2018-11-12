package com.samirsarosh.renderthought.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.samirsarosh.renderthought.data.Picture;

import java.util.List;


/**
 * Created by samirsarosh on 11/11/18.
 */

@Dao
public interface PictureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPictures(List<Picture> pictures);

    @Query("SELECT * FROM Pictures")
    LiveData<List<Picture>> loadAllPictures();

//    @Query("SELECT * FROM movies")
//    LiveData<List<MovieEntity>> loadMovies();
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void saveMovies(List<MovieEntity> movieEntities);
//
//    @Query("SELECT * FROM movies WHERE id=:id")
//    LiveData<MovieEntity> getMovie(int id);
}
