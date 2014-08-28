package eko.ekoapp.com.ekogallery;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

/**
 * Created by cfalc on 8/27/14.
 */
public class PhotoViewAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private ImageFetcher fetcher;
//    public boolean shouldRequestThumb = false;
    private int width;


    public PhotoViewAdapter(Context context) {
        super(context, null, false);
        mInflater = LayoutInflater.from(context);
    }

    public void setImageFetcher(ImageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void setColWidth(int width) {
        this.width = width;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.photo_set_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
        final int id = cursor.getInt(MainActivity.INDEX_ID);
        fetcher.fetch(id, imageView, width);
    }

}
