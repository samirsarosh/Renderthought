package com.samirsarosh.renderthought.data.remote;

import com.samirsarosh.renderthought.data.Picture;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by samirsarosh on 11/11/18.
 */

public interface Webservice {
    /**
     * @GET declares an HTTP GET request
     * @Path("user") annotation on the userId parameter marks it as a
     * replacement for the {user} placeholder in the @GET path
     */
//    @GET("/users/{user}")
//    Call<Picture> getUser(@Path("user") String userId);

    @GET(ApiConstants.PICTURES_URL)
    Call<List<Picture>> getPictures();
}
