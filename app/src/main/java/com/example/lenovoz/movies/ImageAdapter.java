package com.example.lenovoz.movies;

/**
 * Created by lenovo on 4/23/2016.
 */
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by lenovo on 3/25/2016.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<String > mThumbIds;
    private int width;
    private int height;
    public ImageAdapter(Context c, List<String> posters, int width, int height) {
        mThumbIds = posters;
        mContext = c;
        this.width = width;
        this.height = height;
    }

    public int getCount() {
        return mThumbIds.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

//            imageView.setLayoutParams(new GridView.LayoutParams(display.getWidth()*2/5, display.getHeight()*2/5));
            imageView.setLayoutParams(new GridView.LayoutParams(width, height));



            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            imageView.setPadding(8, 8, 8, 8);

        } else {
            imageView = (ImageView) convertView;
        }
        Picasso.with(mContext).load(mThumbIds.get(position)).fit().into(imageView);

        return imageView;
    }

    // references to our images

}

