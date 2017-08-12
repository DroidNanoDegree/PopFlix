package com.sriky.popflix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public class PopularMoviesActivity extends AppCompatActivity implements MoviesDataManager.MovieDataChangedListener {

    //number of columns in the grid.
    private static final int POSTERS_GRID_SPAN = 2;

    /*
     * Handles to the Adaptor and the RecyclerView to aid in reset the list when user toggles btw
     * most_popular and top_rated movies from the settings menu.
     */
    private PopularMoviesAdaptor mPopularMoviesAdaptor;
    private RecyclerView mMoviePosters;
    private MoviesDataManager mMoviesDataManager = MoviesDataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        mMoviesDataManager.loadMovieDataInBackground(MoviesDataManager.QueryType.POPULAR, this.getString(R.string.tmda_api_key));
        mMoviesDataManager.setListener(this);
    }

    @Override
    public void onDataLoadComplete() {
        mMoviePosters = (RecyclerView) findViewById(R.id.rv_posters);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, POSTERS_GRID_SPAN);
        mMoviePosters.setLayoutManager(gridLayoutManager);

        mMoviePosters.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(mMoviesDataManager.getNumberOfItems());
        mMoviePosters.setAdapter(mPopularMoviesAdaptor);
    }

    @Override
    public void onDataLoadFailed(int status) {

    }
}
