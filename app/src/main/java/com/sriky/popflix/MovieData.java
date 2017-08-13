package com.sriky.popflix;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sriky on 8/11/17.
 * Represents movie information gathered from TMDB API.
 */

public class MovieData implements Parcelable {
    //part of the url path for thumbnail poster
    private String mPosterPath;

    //overview or plot synopsis of the movie
    private String mOverview;

    //average vote for the movie.
    private String mVoteAverage;

    //movie id.
    private String mMovieID;

    public MovieData(String posterPath, String overview, String voteAverage, String movieID){
        mPosterPath = posterPath;
        mOverview = overview;
        mVoteAverage = voteAverage;
        mMovieID = movieID;
    }

    public String getPosterPath(){
        return mPosterPath;
    }

    public String getOverview(){
        return mOverview;
    }

    public String getVoteAverage(){
        return mVoteAverage;
    }

    public String getMovieID(){
        return mMovieID;
    }

    public String toString() { return mPosterPath + "--" + mOverview + "--" + mVoteAverage + "--" + mMovieID; }

    protected MovieData(Parcel in) {
        mPosterPath = in.readString();
        mOverview = in.readString();
        mVoteAverage = in.readString();
        mMovieID = in.readString();
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPosterPath);
        dest.writeString(mOverview);
        dest.writeString(mVoteAverage);
        dest.writeString(mMovieID);
    }
}
