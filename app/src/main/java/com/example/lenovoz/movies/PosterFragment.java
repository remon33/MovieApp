package com.example.lenovoz.movies;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PosterFragment extends Fragment {

    private final String API_KEY = "";
    public PosterFragment() {
        // Required empty public constructor
    }

    private GridView gridview;
    private static final String SELECTED_KEY = "selected_position";
    private int mPosition ;
    private boolean itemSelected;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridview = (GridView) rootView.findViewById(R.id.gridview);
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            Log.v("trace", "onview");
        }
        return rootView;
    }



    @Override
    public void onStart() {
        super.onStart();
        FavouriteHelper db = new FavouriteHelper(getContext());
        new FetchMoviesData().execute();
        int position = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(SELECTED_KEY, 0);
        if(mPosition != GridView.INVALID_POSITION){
            gridview.setSelection(mPosition);
            Log.v("trace", "onstart");
        }
    }



    private class FetchMoviesData extends AsyncTask<String ,Void, String> {
        String sortBy;
        @Override
        protected String doInBackground(String... params) {
            String LOG_TAG = FetchMoviesData.class.getSimpleName();

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            try {

                URL url;
                if(getActivity() != null){
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getContext());
                    sortBy = sharedPreferences.getString(
                            getString(R.string.pref_sort_key),
                            getString(R.string.pref_sort_default)
                    );
                    if(sortBy.equals(getString(R.string.pref_sort_most_popular)))
                        url = new URL("https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY);
                    else if(sortBy.equals(getString(R.string.pref_sort_highest_rated)))
                        url = new URL("https://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY);
                    else
                        url = new URL("https://api.themoviedb.org/3/movie/now_playing?api_key=" + API_KEY);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    // Create the request to OpenWeatherMap, and open the connection

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    moviesJsonStr = buffer.toString();
                }




            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return moviesJsonStr;
        }

        @Override
        protected void onPostExecute(String s) {
            FavouriteHelper myDb = new FavouriteHelper(getContext());
            try {
                if(getActivity() == null)
                    return;
                if(sortBy.equals(getString(R.string.pref_sort_favourite))){
                    Cursor res = myDb.getAllData();
                    final ArrayList<String > poster = new ArrayList<String >();
                    final ArrayList<String > title = new ArrayList<String >();
                    final ArrayList<String > releaseDate = new ArrayList<String >();
                    final ArrayList<String > overview = new ArrayList<String >();
                    final ArrayList<String > movieId = new ArrayList<String >();
                    final ArrayList<String > voteAverate = new ArrayList<String >();

                    if(res.getCount() == 0){
                        gridview.setAdapter(null);
                        return;
                    }

                    while (res.moveToNext()){
                        poster.add(res.getString(1));
                        title.add(res.getString(6));
                        releaseDate.add(res.getString(2));
                        overview.add(res.getString(4));
                        movieId.add(res.getString(5));
                        voteAverate.add(res.getString(3));
                    }
                    if(MainActivity.mTwoPane){
                        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                            gridview.setNumColumns(2);
                            gridview.setAdapter(new ImageAdapter(getContext(), poster, gridview.getRight() / 2, gridview.getBottom()/3 ));
                        } else {
                            gridview.setNumColumns(3);
                            gridview.setAdapter(new ImageAdapter(getContext(), poster, gridview.getRight() / 3, gridview.getBottom() / 3));
                        }
                    }
                    else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        gridview.setNumColumns(3);
                        gridview.setAdapter(new ImageAdapter(getContext(), poster, gridview.getRight() / 3, gridview.getBottom() ));
                    }
                    else{
                        gridview.setNumColumns(2);
                        gridview.setAdapter(new ImageAdapter(getContext(), poster, gridview.getRight()/2, gridview.getBottom()/2));
                    }
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String[] movieInfo = {
                                    poster.get(position),
                                    title.get(position),
                                    releaseDate.get(position),
                                    overview.get(position),
                                    movieId.get(position),
                                    voteAverate.get(position)
                            };
                            ((MainActivity)(getActivity())).onItemSelected(movieInfo);
                            mPosition = position;
//                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(SELECTED_KEY, position).commit();

                        }
                    });

                    return;
                }



                final Movies movies = new Movies(s);
                if(MainActivity.mTwoPane){
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        gridview.setNumColumns(2);
                        gridview.setAdapter(new ImageAdapter(getContext(), movies.getPoster_image(), gridview.getRight() / 2, gridview.getBottom() / 3));
                    }else{
                        gridview.setNumColumns(3);
                        gridview.setAdapter(new ImageAdapter(getContext(), movies.getPoster_image(), gridview.getRight() / 3, gridview.getBottom() / 3));
                    }

                }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    gridview.setNumColumns(3);
                    gridview.setAdapter(new ImageAdapter(getContext(), movies.getPoster_image(), gridview.getRight() / 3, gridview.getBottom() ));                }
                else{
                    gridview.setNumColumns(2);
                    gridview.setAdapter(new ImageAdapter(getContext(), movies.getPoster_image(), gridview.getRight() / 2, gridview.getBottom() / 2));
                }

                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String posterURL = movies.getPoster_image().get(position);
                        String[] movieInfo = {
                                posterURL,
                                movies.getTitle().get(position),
                                movies.getReleaseDate().get(position),
                                movies.getOverview().get(position),
                                movies.getId().get(position),
                                movies.getVote().get(position)
                        };

                        ((MainActivity)(getActivity())).onItemSelected(movieInfo);
                        mPosition = position;
                        itemSelected = true;
//                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(SELECTED_KEY, position).commit();

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(MainActivity.mTwoPane && itemSelected){
            if (mPosition != GridView.INVALID_POSITION){
                outState.putInt(SELECTED_KEY, mPosition);
                Log.v("trace", "outstate");
            }
            itemSelected = false;
            return;
        }
        mPosition = gridview.getFirstVisiblePosition();
        outState.putInt(SELECTED_KEY, mPosition);
        Log.v("position", String.valueOf(gridview.getFirstVisiblePosition()));
        super.onSaveInstanceState(outState);
    }

}
