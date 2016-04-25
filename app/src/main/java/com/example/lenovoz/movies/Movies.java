package com.example.lenovoz.movies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 4/18/2016.
 */
public class Movies {
    private List<String> poster_image;
    private List<String> title;
    private List<String> releaseDate;
    private List<String> overview;
    private  List<String> id;
    private List<String> vote;

    public Movies(String moviesData) throws JSONException {
        poster_image = new ArrayList<String>();
        title = new ArrayList<String >();
        releaseDate = new ArrayList<String>();
        overview = new ArrayList<String>();
        id = new ArrayList<String>();
        vote = new ArrayList<String>();
        if(moviesData != null){
            JSONObject moviesjson = new JSONObject(moviesData);
            JSONArray array = moviesjson.getJSONArray("results");

            for(int i = 0; i < array.length(); i++){
                JSONObject oneMovie = array.getJSONObject(i);
                if(oneMovie.getString("poster_path") != null) {
                    poster_image.add("http://image.tmdb.org/t/p/w185" + oneMovie.getString("poster_path"));
                    title.add(oneMovie.getString("original_title"));
                    releaseDate.add(oneMovie.getString("release_date"));
                    overview.add(oneMovie.getString("overview"));
                    id.add(oneMovie.getString("id"));
                    vote.add(oneMovie.getString("vote_average"));
                }
            }
        }


    }

    public List<String> getPoster_image() {
        return poster_image;
    }

    public List<String> getReleaseDate() {
        return releaseDate;
    }

    public List<String> getOverview() {
        return overview;
    }

    public List<String> getTitle() {
        return title;
    }

    public List<String> getId() {
        return id;
    }

    public List<String> getVote() {
        return vote;
    }
}
