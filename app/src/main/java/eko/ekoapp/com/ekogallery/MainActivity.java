package eko.ekoapp.com.ekogallery;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "HOME";
    private static final int LOADER_GALLERY = 1;
    public static final String INTENT_EXTRA_MAX_IMAGE_COUNT = "com.ekoapp.eko.image_count";
    public static final String INTENT_EXTRA_FILE_NAMES = "com.ekoapp.eko.file_names";
    public static final int INDEX_ID = 0;
    public static final int INDEX_DATA = 1;

    private PhotoViewAdapter adapter;
    private Cursor cursor;
    private int imageSelectedCount;
    private int maxImages;
    private AdapterView.OnItemClickListener onItemClickListener = new GridClickListener();
    private Set<String> fileNames = new HashSet<String>();
    private SparseBooleanArray checkStatus = new SparseBooleanArray();
    private final ImageFetcher fetcher = new ImageFetcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSelectedCount = maxImages = getIntent().getIntExtra(INTENT_EXTRA_MAX_IMAGE_COUNT, 1); // default to one

        adapter = new PhotoViewAdapter(this);
        adapter.setImageFetcher(fetcher);
        GridView gridView = (GridView) findViewById(android.R.id.list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(onItemClickListener);

        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int width = p.x;
        gridView.setColumnWidth(width / 3);
        adapter.setColWidth(width / 3);
        gridView.setNumColumns(3);
        if (isExternalStorageReadable()) {
            getLoaderManager().initLoader(LOADER_GALLERY, null, this);
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
//                MediaStore.Files.FileColumns.WIDTH,
//                MediaStore.Files.FileColumns.HEIGHT,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
        };

        // Return only image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                this,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void setChecked(int position, boolean b) {
        checkStatus.put(position, b);
    }

    public boolean isChecked(int position) {
        return checkStatus.get(position);
    }

    public void selectClicked(View ignored) {
        Intent data = new Intent();
        if (fileNames.isEmpty()) {
            this.setResult(RESULT_CANCELED);
        } else {

            ArrayList<String> al = new ArrayList<String>();
            al.addAll(fileNames);
            Bundle res = new Bundle();
            res.putStringArrayList(INTENT_EXTRA_FILE_NAMES, al);

            data.putExtras(res);
            this.setResult(RESULT_OK, data);
        }
        Log.d(TAG, "Returning " + fileNames.size() + " items");
        finish();
    }

    private String getImageName(int position) {
        cursor.moveToPosition(position);
        String name = null;

        try {
            name = cursor.getString(MainActivity.INDEX_DATA);
        } catch (Exception e) {
            return null;
        }
        return name;
    }

    private class GridClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            String name = getImageName(position);

            if (name == null) {
                return;
            }

            if (imageSelectedCount == maxImages) {
                return;
            }

            boolean isChecked = !isChecked(position);

            if (isChecked) {
                if (fileNames.add(name)) {
                    imageSelectedCount++;
                    view.setSelected(true);
                }
            } else {
                if (fileNames.remove(name)) {
                    imageSelectedCount--;
                    view.setSelected(false);
                }
            }

            setChecked(position, isChecked);
        }
    }
}
