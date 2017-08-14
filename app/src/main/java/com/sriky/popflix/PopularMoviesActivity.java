package com.sriky.popflix;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PopularMoviesActivity extends AppCompatActivity implements MoviesDataManager.MovieDataListener {

    //number of columns in the grid.
    private static final int NUMBER_OF_GRID_COLUMNS = 4;

    private static final String TAG = PopularMoviesActivity.class.getSimpleName();

    /*
     * Handles to the Adaptor and the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    private PopularMoviesAdaptor mPopularMoviesAdaptor;
    private RecyclerView mMoviePostersRecyclerView;
    private MoviesDataManager mMoviesDataManager = MoviesDataManager.getInstance();

    private ProgressBar mProgressBar;
    private TextView mErrorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        mMoviePostersRecyclerView = (RecyclerView) findViewById(R.id.rv_posters);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_popularMoviesActivity);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_msg);

        showProgressBarAndHideErrorMessage();

        //calculate the thumbnail width based on the display width of the device, and
        //by the number of grid columns.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int thumbnailWidth = size.x / NUMBER_OF_GRID_COLUMNS;

        //init the MovieDataManager.
        mMoviesDataManager.init(MoviesDataManager.QueryType.POPULAR,
                this.getString(R.string.tmda_api_key), thumbnailWidth, this);
    }

    @Override
    public void onDataLoadComplete() {
        mProgressBar.setVisibility(View.INVISIBLE);//hide the progress bar.

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, NUMBER_OF_GRID_COLUMNS);
        mMoviePostersRecyclerView.setLayoutManager(gridLayoutManager);

        mMoviePostersRecyclerView.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(mMoviesDataManager.getNumberOfItems());
        mMoviePostersRecyclerView.setAdapter(mPopularMoviesAdaptor);
    }

    @Override
    public void onDataLoadFailed(int status) {
        Log.d(TAG, "onDataLoadFailed: status" + status);
        //TODO add refined message and logs.
        hideProgressBarAndShowErrorMessage();

    }

    private void showProgressBarAndHideErrorMessage(){
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBarAndShowErrorMessage(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }
}
