package com.example.lenovoz.movies;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    private final String API_KEY = "";
    static final String DETAIL_URI = "URI";

    FavouriteHelper myDb ;
    public DetailFragment() {
        // Required empty public constructor
    }

    private String[] movieInfo ;
    LinearLayout linearLayout1 ;
    ListView listView1;
    LinearLayout linearLayout2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle arguments = getArguments();
        movieInfo = arguments.getStringArray(DETAIL_URI);

        linearLayout1 = (LinearLayout)rootView.findViewById(R.id.trailer_word);
        listView1 = (ListView) rootView.findViewById(R.id.trailer_list);
        linearLayout2 = (LinearLayout)rootView.findViewById(R.id.inside_scroll_1);

        ImageView imageView = (ImageView)rootView.findViewById(R.id.detail_poster);
        TextView title = (TextView) rootView.findViewById(R.id.movie_title);
        TextView releaseDate = (TextView) rootView.findViewById(R.id.release_date);
        TextView overview = (TextView) rootView.findViewById(R.id.overview);
        TextView vote = (TextView) rootView.findViewById(R.id.vote_average);
        Intent intent = getActivity().getIntent();

            String[] date = movieInfo[2].split("-");
            Picasso.with(getContext()).load(movieInfo[0]).fit().into(imageView);
            title.setText(movieInfo[1]);
            releaseDate.setText(date[0]);
            overview.setText(movieInfo[3]);
            vote.setText(movieInfo[5] + "/10");
        Button button = (Button)rootView.findViewById(R.id.to_favourite);
        final String[] finalMovieInfo = movieInfo;
        myDb = new FavouriteHelper(getContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = myDb.insertData(finalMovieInfo[0], finalMovieInfo[2], finalMovieInfo[5],finalMovieInfo[3], finalMovieInfo[4],finalMovieInfo[1]);
                myDb.deleteDuplicates();
                if (flag)
                    Toast.makeText(getContext(), "Added To Favourite", Toast.LENGTH_SHORT).show();
            }
        });
        new FetchTrailerData().execute(movieInfo[4]);
        new FetchReviewsData().execute(movieInfo[4]);

        return rootView;
    }






    private class FetchTrailerData extends AsyncTask<String, Void, String>
    {
        private final String LOG_TAG = FetchTrailerData.class.getSimpleName();
        @Override
        protected String doInBackground(String... params) {
            String trailerData =  download("https://api.themoviedb.org/3/movie/" + params[0] + "/videos?api_key=" + API_KEY);
            return trailerData;
        }



        @Override
        protected void onPostExecute(String s) {
            try {
                final ArrayList<String > youtubeUrl = new ArrayList<String>();
                ArrayList<String > trailerNo = new ArrayList<String>();
                JSONObject jsonObject = new JSONObject(s);
                JSONArray array = jsonObject.getJSONArray("results");
                for (int i = 0; i < array.length(); i++){
                    JSONObject getKey = array.getJSONObject(i);
                    String key = getKey.getString("key");
                    youtubeUrl.add("https://www.youtube.com/watch?v=" + key);
                }
                if(youtubeUrl.isEmpty()){
                    //no trailer to display
                    return;
                }
                if(getActivity() == null){
                    return;
                }
                TextView reviewText = new TextView(getContext());
                reviewText.setText("Trailer:");
                reviewText.setTextColor(Color.rgb(0, 0, 0));
                reviewText.setBackgroundColor(Color.rgb(255, 255, 255));
                linearLayout1.addView(reviewText);

                for(int i = 0; i < array.length(); i++){
                    int j = i+1;
                    trailerNo.add("trailer" + j);
                }

                ArrayAdapter arrayAdapter = new ArrayAdapter(
                        getActivity(),
                        R.layout.trailer_item,
                        R.id.trailer_item_text,
                        trailerNo
                );

                listView1.setAdapter(arrayAdapter);
                setListViewHeightBasedOnChildren(listView1);
                listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(youtubeUrl.get(position)));
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });



            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        private String download(String urlparam){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                URL url = new URL(urlparam);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                trailerJsonStr = buffer.toString();


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

            // This will only happen if there was an error getting or parsing the forecast.
            return trailerJsonStr;
        }

    }


    private class FetchReviewsData extends AsyncTask<String, Void, String>
    {
        private final String LOG_TAG = FetchReviewsData.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String reviewsJson = download("https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?api_key=" + API_KEY);
            return reviewsJson;
        }

        @Override
        protected void onPostExecute(String s) {
            List<String > reviews = new ArrayList<String>();
            try {
                JSONObject moviesjson = new JSONObject(s);
                if(moviesjson.getInt("total_results") == 0) return;
                JSONArray array = moviesjson.getJSONArray("results");
                for(int i = 0; i < array.length(); i++){
                    JSONObject review = array.getJSONObject(i);
                    reviews.add(review.getString("content"));
                }

                if(getActivity() == null){
                    return;
                }

                for(int i = 0; i < array.length(); i++){
                    TextView reviewText = new TextView(getContext());
                    reviewText.setText(reviews.get(i));
                    reviewText.setTextColor(Color.rgb(0, 0, 0));
                    reviewText.setBackgroundColor(Color.rgb(255, 255, 255));
                    reviewText.setPadding(10, 10, 10, 10);
                    linearLayout2.addView(reviewText);
                    // put line between each review
                    View v = new View(getActivity());
                    v.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            5
                    ));
                    v.setBackgroundColor(Color.parseColor("#B3B3B3"));

                    linearLayout2.addView(v);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String download(String urlparam){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewsJsonStr = null;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                URL url = new URL(urlparam);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                reviewsJsonStr = buffer.toString();


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

            // This will only happen if there was an error getting or parsing the forecast.
            return reviewsJsonStr;
        }
    }


    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }




}
