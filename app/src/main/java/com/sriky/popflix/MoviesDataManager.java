package com.sriky.popflix;

import android.os.AsyncTask;
import android.util.Log;

import com.sriky.popflix.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sriky on 8/11/17.
 */

public class MoviesDataManager {
    /*
     * Singleton management code
     */
    private static final MoviesDataManager mMoviesDataManagerInstance = new MoviesDataManager();
    private MoviesDataManager() {
    }

    /**
     * Type of Query to make, such that results are organized by type specified.
     */
    public enum QueryType{
        POPULAR,
        TOP_RATED
    }

    private static final String TAG = MoviesDataManager.class.getSimpleName();

    //TMDB query - json keys.
    private static final String JSON_KEY_ARRAY_RESULTS = "results";
    private static final String JSON_KEY_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_OVERVIEW = "overview";
    private static final String JSON_KEY_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_MOVIE_ID = "id";

    //listener object
    private MovieDataChangedListener mMovieDataChangedListener;
    //list to hold the downloaded movie data.
    private ArrayList<MovieData> mMovieDataArrayList = new ArrayList<>();
    //the Uri path that determine the width of the poster thumbnail.
    private String mQueryThumbnailWidthPath;

    /**
     * Returns the instance.
     *
     * @return instance of MoviesDataManager
     */
    public static MoviesDataManager getInstance() { return mMoviesDataManagerInstance; }

    /**
     * Initializes the MoviesDataManager and downloads the movie data from TMDB.
     *
     * @param type - specify the supported type of ordering for the movies.
     * @param api_key - TMDB API Key
     * @param thumbnailWidth - desired width for the movie poster thumbnails.
     * @param listener - where the callbacks should be routed to.
     */
    public void init(QueryType type, String api_key, int thumbnailWidth, MovieDataChangedListener listener){
        //set the listener.
        mMovieDataChangedListener = listener;

        //set the Uri path for the supported width.
        calculateThumbnailSizeToDownload(thumbnailWidth);

        //trigger the async task to download movie data.
        TMDAQueryTask tmdaQueryTask = new TMDAQueryTask();
        if(type == QueryType.POPULAR){
            tmdaQueryTask.execute(NetworkUtils.getURLForPopularMovies(api_key));
        }else if(type == QueryType.TOP_RATED){
            tmdaQueryTask.execute(NetworkUtils.getURLForTopRatedMovies(api_key));
        }
    }

    /**
     * Gets the relative image path from TMDB for a specific index.
     *
     * @param index of the image.
     * @return - relative path.
     */
    public String getImageRelativePathAtIndex(int index){
        //TODO add error checks.

        return mMovieDataArrayList.get(index).getPosterPath();
    }

    /**
     * The Uri path that specifies the width of the thumbnail to be downloaded.
     * @return
     */
    public String getThumbnailWidthPath(){
        return mQueryThumbnailWidthPath;
    }

    /**
     * Gets the MovieID for the specified index.
     *
     * @param index index from the movie data list.
     * @return movieID
     */
    public String getMovieIdAtIndex(int index){
        //TODO add error checks.

        return mMovieDataArrayList.get(index).getMovieID();
    }

    /**
     * Get the size of elements downloaded from TMDB.
     *
     * @return total number of the elements available to be displayed.
     */
    public int getNumberOfItems(){
        return mMovieDataArrayList.size();
    }

    /*
     * Sets the width for the thumbnail to be downloaded to the supported sizes by TMDB.
     */
    private void calculateThumbnailSizeToDownload(int thumbnailWidth){
        if(thumbnailWidth > 0 && thumbnailWidth <= 92){
            mQueryThumbnailWidthPath = "w92";
        }else if(thumbnailWidth > 92 && thumbnailWidth <= 154){
            mQueryThumbnailWidthPath = "w154";
        }else if(thumbnailWidth > 154 && thumbnailWidth <= 185){
            mQueryThumbnailWidthPath = "w185";
        }else if(thumbnailWidth > 185 && thumbnailWidth <= 342){
            mQueryThumbnailWidthPath = "w342";
        }else  if(thumbnailWidth > 342 && thumbnailWidth <= 500){
            mQueryThumbnailWidthPath = "w500";
        }else if(thumbnailWidth > 500){
            mQueryThumbnailWidthPath = "w780";
        }
    }

    /*
     * Interface for the listener.
     */
    interface MovieDataChangedListener {
        //occurs when data is downloaded successfully.
        void onDataLoadComplete();
        //occurs when there was issues downloading data.
        void onDataLoadFailed(int status);
    }

    private class TMDAQueryTask extends AsyncTask<URL, Void, String>{

        @Override
        protected String doInBackground(URL... params) {
            URL url = params[0];
            String results = null;
            try{
                results = NetworkUtils.getStringResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String queryResult){
            if(queryResult != null) {
                Log.d(TAG, "onPostExecute: "+queryResult);
                // TODO check response and process if response == 200.
                try {
                    JSONObject moviesData = new JSONObject(queryResult);
                    JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_KEY_ARRAY_RESULTS);
                    if (jsonArrayResults != null) {
                        for (int i = 0; i < jsonArrayResults.length(); i++) {
                            JSONObject data = (JSONObject) jsonArrayResults.get(i);
                            MovieData movieData = new MovieData(data.getString(JSON_KEY_POSTER_PATH),
                                    data.getString(JSON_KEY_OVERVIEW), data.getString(JSON_KEY_VOTE_AVERAGE),
                                    data.getString(JSON_KEY_MOVIE_ID));
                            mMovieDataArrayList.add(movieData);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mMovieDataChangedListener.onDataLoadFailed(0);
                }
            }
            mMovieDataChangedListener.onDataLoadComplete();
        }
    }
}
