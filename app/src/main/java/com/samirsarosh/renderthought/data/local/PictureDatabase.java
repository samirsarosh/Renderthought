package com.samirsarosh.renderthought.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.samirsarosh.renderthought.data.Picture;

/**
 * Created by samirsarosh on 11/12/18.
 */

@Database(entities = {Picture.class}, version = 1)
public abstract class PictureDatabase extends RoomDatabase {
    public abstract PictureDao pictureDao();
}
