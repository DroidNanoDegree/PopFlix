package com.sriky.popflix;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sriky.popflix.utilities.MovieDataHelper;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.prefs.PreferenceChangeEvent;

import static android.R.attr.type;

public class PopularMoviesActivity extends AppCompatActivity{

    //number of columns in the grid.
    private static final int NUMBER_OF_GRID_COLUMNS = 4;

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    /*
     * Handles to the Adaptor and the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    private PopularMoviesAdaptor mPopularMoviesAdaptor;
    private RecyclerView mMoviePostersRecyclerView;

    private ProgressBar mProgressBar;
    private TextView mErrorMessageTextView;

    //list to hold the downloaded movie data.
    private ArrayList<MovieData> mMovieDataArrayList = new ArrayList<>();
    //the Uri path that determine the width of the poster thumbnail.
    private String mQueryThumbnailWidthPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        mMoviePostersRecyclerView = (RecyclerView) findViewById(R.id.rv_posters);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_popularMoviesActivity);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_msg);

        showProgressBarAndHideErrorMessage();

        setupQueryThumbnailWidthPath();

        setupSharedPreferences();
    }

    /**
     * calculate the thumbnail width based on the display width of the device and
     * by the number of grid columns.
     */
    private void setupQueryThumbnailWidthPath(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int thumbnailWidth = size.x / NUMBER_OF_GRID_COLUMNS;
        mQueryThumbnailWidthPath = MovieDataHelper.getThumbnailQueryPath(thumbnailWidth);
        Log.d(TAG, "setupQueryThumbnailWidthPath: width Path = "+mQueryThumbnailWidthPath);
    }

    private void setupSharedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = getString(R.string.tmdb_api_key);

        String sortingOrder = sharedPreferences.getString(getString(R.string.sort_order_key), getString(R.string.default_sort_order));
        Log.d(TAG, "setupSharedPreferences: sortingOrder = "+sortingOrder);

        //trigger the async task to download movie data.
        TMDAQueryTask tmdaQueryTask = new TMDAQueryTask();
        tmdaQueryTask.execute(NetworkUtils.buildURL(sortingOrder, apiKey));
    }

    private void showProgressBarAndHideErrorMessage(){
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBarAndShowErrorMessage(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }


    private void onDataLoadComplete() {
        Log.d(TAG, "onDataLoadComplete()");
        mProgressBar.setVisibility(View.INVISIBLE);//hide the progress bar.

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, NUMBER_OF_GRID_COLUMNS);
        mMoviePostersRecyclerView.setLayoutManager(gridLayoutManager);

        mMoviePostersRecyclerView.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(getNumberOfItems());
        mMoviePostersRecyclerView.setAdapter(mPopularMoviesAdaptor);
    }

    private void onDataLoadFailed(int status) {
        Log.d(TAG, "onDataLoadFailed: status" + status);
        //TODO add refined message and logs.
        hideProgressBarAndShowErrorMessage();
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

    private class TMDAQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            showProgressBarAndHideErrorMessage();
        }

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
                Log.d(TAG, "onPostExecute: queryResult.length() = "+queryResult.length());
                // TODO check response and process if response == 200.
                mMovieDataArrayList = MovieDataHelper.getListfromJSONResponse(queryResult);
            }else{
                onDataLoadFailed(0);
            }
            onDataLoadComplete();
        }
    }
}
