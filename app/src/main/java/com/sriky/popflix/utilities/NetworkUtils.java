package com.sriky.popflix.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by sriky on 8/11/17.
 */

public final class NetworkUtils {
    private static final String TMDA_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String PATH_POPULAR_MOVIES = "popular";
    private static final String PATH_TOP_MOVIES = "top_rated";
    private static final String PARAM_QUERY_API_KEP = "api_key";

    //images
    private static final String TMDA_IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    private static final String PATH_IMAGE_PATH = "images";
    private static final String PATH_IMAGE_SIZE = "w185";

    /**
     * Build and returns URL.
     *
     * @return URL to get Popular Movies.
     */
    public static URL getURLForPopularMovies(String apiKey){
        return getURL(PATH_POPULAR_MOVIES, apiKey);
    }

    /**
     * Build and returns URL for Top Rated Movies.
     *
     * @return URL to get Top Rated Movies.
     */
    public static URL getURLForTopRatedMovies(String apiKey){
        return getURL(PATH_TOP_MOVIES, apiKey);
    }

    /**
     * Builds and returns Uri from the supplied relative path.
     *
     * @param relativePath - relative path location at TMDB.
     * @return - complete query Uri to TMDB, where image size is w185.
     */
    public static Uri getURLForImageWithRelativePath(String relativePath){
        Uri uri = Uri.parse(TMDA_IMAGE_BASE_URL).buildUpon()
                .appendPath(PATH_IMAGE_SIZE)
                .appendPath(relativePath)
                .build();

        return uri;
    }

    public static Uri getUriForMovieIDwithRelativePath(String movieId, String relativePath, String apiKey){
        Uri uri = Uri.parse(TMDA_BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath(PATH_IMAGE_PATH)
                .appendQueryParameter(PARAM_QUERY_API_KEP, apiKey)
                .build();

        return uri;
    }


    public static URL getURL(String path, String apiKey){
        Uri uri = Uri.parse(TMDA_BASE_URL).buildUpon()
                .appendPath(path)
                .appendQueryParameter(PARAM_QUERY_API_KEP, apiKey)
                .build();

        URL url = null;
        try{
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The String contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getStringResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
