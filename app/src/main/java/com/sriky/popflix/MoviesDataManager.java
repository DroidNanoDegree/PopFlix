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

    private static final String JSON_ARRAY_RESULTS = "results";
    private static final String JSON_POSTER_PATH = "poster_path";
    private static final String JSON_OVERVIEW = "overview";
    private static final String JSON_VOTE_AVERAGE = "vote_average";
    private static final String JSON_MOVIE_ID = "id";

    //listener object
    private MovieDataChangedListener mMovieDataChangedListener;

    private ArrayList<MovieData> mMovieDataArrayList = new ArrayList<>();

    /**
     * Returns the instance.
     *
     * @return instance of MoviesDataManager
     */
    public static MoviesDataManager getInstance() { return mMoviesDataManagerInstance; }

    /**
     * Sets the listener
     *
     * @param listener - will receive the following callbacks:
     *     onDataLoadComplete() - upon successful download.
     *     onDataLoadFailed(status) - when download fails with appreciate status.
     */
    public void setListener(MovieDataChangedListener listener) {
        mMovieDataChangedListener = listener;
    }

    /**
     * Query TMD, ordering based on the type specified.
     *
     * @param type movies will be ordered on the type specified.
     * @param api_key api_key to TMD.
     */
    public void loadMovieDataInBackground(QueryType type, String api_key){
        TMDAQueryTask tmdaQueryTask = new TMDAQueryTask();
        if(type == QueryType.POPULAR){
            tmdaQueryTask.execute(NetworkUtils.getURLForPopularMovies(api_key));
        }else if(type == QueryType.TOP_RATED){
            tmdaQueryTask.execute(NetworkUtils.getURLForTopRatedMovies(api_key));
        }
    }

    /**
     * Gets the relative image path from TMDB for a specific index.
     * @param index of the image.
     * @return - relative path.
     */
    public String getImageRelativePathAtIndex(int index){
        //TODO add error checks.

        return mMovieDataArrayList.get(index).getPosterPath();
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
     * @return size of the elements.
     */
    public int getNumberOfItems(){
        return mMovieDataArrayList.size();
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
                    JSONArray jsonArrayResults = moviesData.getJSONArray(JSON_ARRAY_RESULTS);
                    if (jsonArrayResults != null) {
                        for (int i = 0; i < jsonArrayResults.length(); i++) {
                            JSONObject data = (JSONObject) jsonArrayResults.get(i);
                            MovieData movieData = new MovieData(data.getString(JSON_POSTER_PATH),
                                    data.getString(JSON_OVERVIEW), data.getString(JSON_VOTE_AVERAGE),
                                    data.getString(JSON_MOVIE_ID));
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
