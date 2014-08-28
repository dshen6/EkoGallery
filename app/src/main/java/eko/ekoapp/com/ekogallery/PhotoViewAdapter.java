package eko.ekoapp.com.ekogallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
    private LoaderCompatShim<Cursor> mDrawableFactory;

    public PhotoViewAdapter(Context context) {
        super(context, null, false);
        mInflater = LayoutInflater.from(context);
    }
    public void setDrawableFactory(LoaderCompatShim<Cursor> factory) {
        mDrawableFactory = factory;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.photo_set_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView iv = (ImageView) view.findViewById(R.id.thumbnail);
        Drawable recycle = iv.getDrawable();
        Drawable drawable = mDrawableFactory.drawableForItem(cursor, recycle);
        if (recycle != drawable) {
            iv.setImageDrawable(drawable);
        }
    }
}
