package com.sriky.popflix;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;

public class PopularMoviesActivity extends AppCompatActivity implements MoviesDataManager.MovieDataChangedListener {

    //number of columns in the grid.
    private static final int NUMBER_OF_GRID_COLUMNS = 4;

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
        mMoviePosters = (RecyclerView) findViewById(R.id.rv_posters);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, NUMBER_OF_GRID_COLUMNS);
        mMoviePosters.setLayoutManager(gridLayoutManager);

        mMoviePosters.setHasFixedSize(true);

        mPopularMoviesAdaptor = new PopularMoviesAdaptor(mMoviesDataManager.getNumberOfItems());
        mMoviePosters.setAdapter(mPopularMoviesAdaptor);
    }

    @Override
    public void onDataLoadFailed(int status) {

    }
}
