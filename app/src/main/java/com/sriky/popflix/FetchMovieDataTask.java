package com.sriky.popflix;

import android.os.AsyncTask;
import android.util.Log;

import com.sriky.popflix.utilities.MovieDataHelper;
import com.sriky.popflix.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * {@link AsyncTask} to fetch movie data from the supplied URL and returns the string results.
 * In-order to receive callbacks for onPreExecute & onFetchSuccess, the calling
 * class needs to implement either FetchBasicMovieDataTaskListener or FetchFullMovieDataTaskListener
 * based on the expected results.
 */

public class FetchMovieDataTask extends AsyncTask<URL, Void, String> {
    /**
     * Base Interface for routing async task callbacks.
     */
    interface FetchMovieDataTaskListener {
        void onPreExecute();
        void onFetchFailed();
    }

    /**
     * Interface to route basic results.
     */
    interface FetchBasicMovieDataTaskListener extends FetchMovieDataTaskListener{
        void onFetchSuccess(List<MovieData> movieDataList);
    }

    /**
     * Interface to route full movie data result.
     */
    interface FetchFullMovieDataTaskListener extends FetchMovieDataTaskListener{
        void onFetchSuccess(MovieData movieData);
    }

    private static final String TAG = FetchMovieDataTask.class.getSimpleName();

    private FetchMovieDataTaskListener mFetchMovieDataTaskListener;

    public FetchMovieDataTask(FetchMovieDataTaskListener fetchMovieDataTaskListener){
        mFetchMovieDataTaskListener = fetchMovieDataTaskListener;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute()");
        mFetchMovieDataTaskListener.onPreExecute();
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
            if(mFetchMovieDataTaskListener instanceof FetchBasicMovieDataTaskListener){
                ((FetchBasicMovieDataTaskListener)mFetchMovieDataTaskListener).onFetchSuccess
                        ((MovieDataHelper.getListfromJSONResponse(queryResult)));
            }else if(mFetchMovieDataTaskListener instanceof FetchFullMovieDataTaskListener){
                ((FetchFullMovieDataTaskListener)mFetchMovieDataTaskListener).onFetchSuccess
                        (MovieDataHelper.getMovieDataFrom(queryResult));
            }
        } else {
            mFetchMovieDataTaskListener.onFetchFailed();
        }
    }
}
