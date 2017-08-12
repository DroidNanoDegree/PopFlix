package com.sriky.popflix;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.sriky.popflix.utilities.NetworkUtils;

/**
 * Created by sriky on 8/11/17.
 */

public class PopularMoviesAdaptor extends RecyclerView.Adapter<PopularMoviesAdaptor.ImageViewHolder> {

    private static final String TAG = PopularMoviesAdaptor.class.getSimpleName();

    //total number of movie posters that will in the grid layout.
    private int mNumberOfItems;

    public PopularMoviesAdaptor(int numberOfItems){
        mNumberOfItems = numberOfItems;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.popularmovies_list_item, parent, false);

        ImageViewHolder imageViewHolder = new ImageViewHolder(view);
        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        //will display the image poster/thumbnail.
        ImageView mMovieThumbNailView;

        //handle to MoviesDataManager
        private final MoviesDataManager mMoviesDataManager = MoviesDataManager.getInstance();

        public ImageViewHolder(View itemView) {
            super(itemView);

            mMovieThumbNailView = (ImageView) itemView.findViewById(R.id.iv_movie_thumbnail);
        }

        /**
         * Method to set the poster image from the list of posters.
         *
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            Context context = mMovieThumbNailView.getContext();
            String relativePath = mMoviesDataManager.getImageRelativePathAtIndex(listIndex);
            Uri uri = NetworkUtils.getURLForImageWithRelativePath(relativePath);
            Log.d(TAG, "bind: "+listIndex+", uri"+uri.toString());
            Picasso.with(context).load(uri).into(mMovieThumbNailView);
        }
    }
}
