package com.sriky.popflix;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.utilities.MovieDataHelper;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailActivity.class.getSimpleName();
    private static final String PARCEL_KEY = "movie_data";

    private ProgressBar mProgressBar;
    private TextView mErrorMessageTextView;

    private ImageView mMoviePosterImageView;
    private TextView mMovieTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mOverviewTextView;
    private RatingBar mRatingsBar;

    private MovieData mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_details_activity);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_details_activity_error_msg);
        mErrorMessageTextView.setText( getString(R.string.data_download_error) );

        mMoviePosterImageView = (ImageView) findViewById(R.id.iv_details_thumbnail);
        mMovieTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        mOverviewTextView = (TextView) findViewById(R.id.tv_overview);
        mRatingsBar = (RatingBar) findViewById(R.id.rb_ratings);

        setMoviePosterImageHeigth();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState != null && savedInstanceState.containsKey(PARCEL_KEY)){
            mMovieData = savedInstanceState.getParcelable(PARCEL_KEY);
            onDownloadSuccess();
        }else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(MovieDataHelper.MOVIE_ID_INTENT_EXTRA_KEY)) {
                String movieID = intent.getStringExtra(MovieDataHelper.MOVIE_ID_INTENT_EXTRA_KEY);
                URL url = NetworkUtils.buildURL(movieID, getString(R.string.tmdb_api_key));
                QueryMovieDetailsTask queryTask = new QueryMovieDetailsTask();
                queryTask.execute(url);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PARCEL_KEY, mMovieData);
        super.onSaveInstanceState(outState);
    }

    private void setMoviePosterImageHeigth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mMoviePosterImageView.getLayoutParams().height = size.y / 2;
    }

    private void onDownloadSuccess(){
        //hide the progress bar.
        mProgressBar.setVisibility(View.INVISIBLE);
        //hide error msg tv.
        mErrorMessageTextView.setVisibility(View.INVISIBLE);

        String relativePath = mMovieData.getPosterPath();
        Uri uri = NetworkUtils.getURLForImageWithRelativePathAndSize(relativePath, MovieDataHelper.getQueryThumbnailWidthPath());
        Picasso.with(this).load(uri).into(mMoviePosterImageView);

        mReleaseDateTextView.setText(mMovieData.getReleaseDate());
        mOverviewTextView.setText(mMovieData.getOverview());
        mMovieTitleTextView.setText(mMovieData.getTitle());
        float ratings = Float.parseFloat(mMovieData.getVoteAverage()) / 2;
        mRatingsBar.setRating(ratings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private class QueryMovieDetailsTask extends AsyncTask<URL, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show the progress bar.
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = params[0];
            Log.d(TAG, "doInBackground: Querying URL = "+url);
            String result = null;
            try{
                result = NetworkUtils.getStringResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //hide the progress bar.
            mProgressBar.setVisibility(View.INVISIBLE);
            if(result != null){
                mMovieData = MovieDataHelper.getMovieDataFrom(result);
                onDownloadSuccess();
            }else{
                mErrorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}
