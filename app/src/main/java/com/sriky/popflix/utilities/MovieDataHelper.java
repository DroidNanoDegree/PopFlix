package com.sriky.popflix.utilities;

import android.util.Log;

import com.sriky.popflix.MovieData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sriky on 8/14/17.
 */

public final class MovieDataHelper {

    public static final String MOVIE_ID_INTENT_EXTRA_KEY = "movie_id";

    private static final String TAG = MovieDataHelper.class.getSimpleName();

    //TMDB query - json keys.
    private static final String JSON_KEY_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_MOVIE_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_MOVIE_OVERVIEW = "overview";
    private static final String JSON_KEY_MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_MOVIE_ID = "id";
    private static final String JSON_KEY_MOVIE_TITLE = "title";
    private static final String JSON_KEY_MOVIE_RELEASE_DATE = "release_date";

    /**
     * Returns the closest possible width query path supported by TMDB.
     *
     * @param thumbnailWidth - desired width to display movie posters.
     * @return query widthPath.
     */
    public static String getThumbnailQueryPath(int thumbnailWidth){
        if(thumbnailWidth <= 0){
            Log.w(TAG, "getThumbnailQueryPath: thumnailWidth = "+thumbnailWidth+" in incorrect, will use default w185!");
        }

        String widthPath = "w185";//default.
        if(thumbnailWidth > 0 && thumbnailWidth <= 92){
            widthPath = "w92";
        }else if(thumbnailWidth > 92 && thumbnailWidth <= 154){
            widthPath = "w154";
        }else if(thumbnailWidth > 154 && thumbnailWidth <= 185){
            widthPath = "w185";
        }else if(thumbnailWidth > 185 && thumbnailWidth <= 342){
            widthPath = "w342";
        }else  if(thumbnailWidth > 342 && thumbnailWidth <= 500){
            widthPath = "w500";
        }else if(thumbnailWidth > 500){
            widthPath = "w780";
        }
        return widthPath;
    }

    public static MovieData getMovieDataFrom(String queryResult){
        MovieData movieData = new MovieData();
        try {
            JSONObject movieDetails = new JSONObject(queryResult);
            movieData.setPosterPath( movieDetails.getString(JSON_KEY_MOVIE_POSTER_PATH) );
            movieData.setOverview( movieDetails.getString(JSON_KEY_MOVIE_OVERVIEW) );
            movieData.setTitle( movieDetails.getString(JSON_KEY_MOVIE_TITLE) );
            movieData.setReleaseDate( movieDetails.getString(JSON_KEY_MOVIE_RELEASE_DATE) );
            movieData.setVoteAverage( movieDetails.getString(JSON_KEY_MOVIE_VOTE_AVERAGE) );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieData;
    }

    public static ArrayList<MovieData> getListfromJSONResponse(String queryResult){
        ArrayList<MovieData> movieDataList = new ArrayList<>();
        try {
            JSONObject moviesData = new JSONObject(queryResult);
            JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_KEY_ARRAY_RESULTS);
            if (jsonArrayResults != null) {
                for (int i = 0; i < jsonArrayResults.length(); i++) {
                    JSONObject data = (JSONObject) jsonArrayResults.get(i);
                    MovieData movieData = new MovieData();
                    movieData.setPosterPath( data.getString(JSON_KEY_MOVIE_POSTER_PATH) );
                    movieData.setMovieID( data.getString(JSON_KEY_MOVIE_ID) );
                    movieDataList.add(movieData);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieDataList;
    }
}
