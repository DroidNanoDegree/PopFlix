package com.sriky.popflix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sriky.popflix.settings.SettingsActivity;
import com.sriky.popflix.utilities.MovieDataHelper;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main Activity that is launched from the Launcher.
 * Responsible for querying TMDB's APIs and displaying movies in the specified order.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, PopularMoviesAdaptor.MoviePosterOnClickEventListener {

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    //number of columns in the grid.
    private static final int NUMBER_OF_GRID_COLUMNS = 4;
    //key for saving and retrieving data from savedInstanceState.
    private static final String MOVIE_DATA_LIST_KEY = "movie_data_list";

    /*
     * Handles to the Adaptor and the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    private PopularMoviesAdaptor mPopularMoviesAdaptor;
    public @BindView(R.id.rv_posters) RecyclerView mMoviePostersRecyclerView;

    public @BindView(R.id.pb_popularMoviesActivity) ProgressBar mProgressBar;
    public @BindView(R.id.tv_error_msg) TextView mErrorMessageTextView;

    //list to hold the downloaded movie data.
    private ArrayList<MovieData> mMovieDataArrayList;

    //query parameter for sorting ordering.
    private String mSortingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        ButterKnife.bind(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(PopularMoviesActivity.this, NUMBER_OF_GRID_COLUMNS);
        mMoviePostersRecyclerView.setLayoutManager(gridLayoutManager);

        mMoviePostersRecyclerView.setHasFixedSize(true);

        mMovieDataArrayList = new ArrayList<>();

        showProgressBarAndHideErrorMessage();

        setupQueryThumbnailWidthPath();

        setSortingOrderFromSharedPreferences();

        //if saved data exists, then use the movie data that was already downloaded.
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_DATA_LIST_KEY)) {
            Log.d(TAG, "onCreate: loading data from savedInstanceState()");
            mMovieDataArrayList = savedInstanceState.getParcelableArrayList(MOVIE_DATA_LIST_KEY);
            onDataLoadComplete();
        } else {
            Log.d(TAG, "onCreate: movie data must be downloaded!");
            //trigger the async task to download movie data.
            downloadMovieDataInBackground();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");
        outState.putParcelableArrayList(MOVIE_DATA_LIST_KEY, mMovieDataArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sort_order_key))) {
            mSortingOrder = sharedPreferences.getString(key, getString(R.string.default_sort_order));
            Log.d(TAG, "onSharedPreferenceChanged: mSortingOrder = " + mSortingOrder);
            mMovieDataArrayList.clear();
            downloadMovieDataInBackground();
        }
    }

    @Override
    public void onClickedItemAt(int index) {
        try {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDataHelper.MOVIE_ID_INTENT_EXTRA_KEY,
                    mMovieDataArrayList.get(index).getMovieID());
            startActivity(intent);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Starts an AsyncTask to download data in the background.
     */
    private void downloadMovieDataInBackground() {
        Log.d(TAG, "downloadMovieDataInBackground()");
        TMDAQueryTask tmdaQueryTask = new TMDAQueryTask();
        tmdaQueryTask.execute(NetworkUtils.buildURL(mSortingOrder, getString(R.string.tmdb_api_key)));
    }

    /**
     * calculate the thumbnail width based on the display width of the device and
     * by the number of grid columns.
     */
    private void setupQueryThumbnailWidthPath() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int thumbnailWidth = size.x / NUMBER_OF_GRID_COLUMNS;
        MovieDataHelper.setThumbnailQueryPath(thumbnailWidth);
    }

    /**
     * Sets the mSortingOrder variable to appropriate value from sharedPreference, if any.
     * Otherwise "popular" will be set as the default.
     */
    private void setSortingOrderFromSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSortingOrder = sharedPreferences.getString(getString(R.string.sort_order_key), getString(R.string.default_sort_order));
        Log.d(TAG, "setSortingOrderFromSharedPreferences: sortingOrder = " + mSortingOrder);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Displays the progress bar and hides the error message views.
     */
    private void showProgressBarAndHideErrorMessage() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides the progress bar view and makes the the error message view VISIBLE.
     */
    private void hideProgressBarAndShowErrorMessage() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    /**
     * On successfully downloading data from TMDB
     * a new instance of PopularMoviesAdaptor is created based on the number of items
     * in the mMovieDataArrayList.
     * The new instance of PopularMoviesAdaptor is set to the mMoviePostersRecyclerView.
     */
    private void onDataLoadComplete() {
        Log.d(TAG, "onDataLoadComplete()");
        mProgressBar.setVisibility(View.INVISIBLE);//hide the progress bar.
        mPopularMoviesAdaptor = new PopularMoviesAdaptor(getNumberOfItems(), this);
        mMoviePostersRecyclerView.setAdapter(mPopularMoviesAdaptor);
    }

    /**
     * If there were issues downloading data from TMDB, then hide the progress bar view and
     * display an error message to the user.
     *
     * @param status error status code.
     */
    private void onDataLoadFailed(int status) {
        Log.d(TAG, "onDataLoadFailed: status code" + status);
        //TODO - Nice to have refined message.
        hideProgressBarAndShowErrorMessage();
    }

    /**
     * Gets the relative image path from TMDB for a specific index.
     *
     * @param index of the image.
     * @return relative path.
     * @throws ArrayIndexOutOfBoundsException
     */
    public String getImageRelativePathAtIndex(int index) throws ArrayIndexOutOfBoundsException {
        return mMovieDataArrayList.get(index).getPosterPath();
    }

    /**
     * Get the size of elements downloaded from TMDB.
     *
     * @return total number of the elements available to be displayed.
     */
    public int getNumberOfItems() {
        return mMovieDataArrayList.size();
    }

    private class TMDAQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute()");
            showProgressBarAndHideErrorMessage();
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = params[0];
            Log.d(TAG, "doInBackground: URL = " + url);
            String results = null;
            try {
                results = NetworkUtils.getStringResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String queryResult) {
            if (queryResult != null) {
                Log.d(TAG, "onPostExecute: queryResult.length() = " + queryResult.length());
                mMovieDataArrayList.addAll(MovieDataHelper.getListfromJSONResponse(queryResult));
                onDataLoadComplete();
            } else {
                onDataLoadFailed(0);
            }
        }
    }
}
