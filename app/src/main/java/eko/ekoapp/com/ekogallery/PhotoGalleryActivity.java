package eko.ekoapp.com.ekogallery;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class PhotoGalleryActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, PhotoViewAdapter.IsCheckedProvider {

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
    private SparseBooleanArray checkStatusPos = new SparseBooleanArray();
    private SparseBooleanArray checkStatusId = new SparseBooleanArray();
    private final ImageFetcher fetcher = new ImageFetcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        maxImages = getIntent().getIntExtra(INTENT_EXTRA_MAX_IMAGE_COUNT, 1); // default to one

        adapter = new PhotoViewAdapter(this);
        adapter.setImageFetcher(fetcher);
        adapter.setCheckedProvider(this);
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

    private void setChecked(int position, int id, boolean b) {
        checkStatusPos.put(position, b);
        checkStatusId.put(id, b);
    }

    public boolean isCheckedId(int id) {
        return checkStatusId.get(id);
    }

    public boolean isCheckedPos(int position) {
        return checkStatusPos.get(position);
    }

    public void finishActivity() {
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
            name = cursor.getString(PhotoGalleryActivity.INDEX_DATA);
        } catch (Exception e) {
            return null;
        }
        return name;
    }

    private int getImageId(int position) {
        cursor.moveToPosition(position);
        return cursor.getInt(PhotoGalleryActivity.INDEX_ID);
    }

    private class GridClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            String name = getImageName(position);
            int id = getImageId(position);

            if (name == null) {
                return;
            }

            boolean isChecked = !isCheckedPos(position);

            if (imageSelectedCount == maxImages && isChecked) {
                return;
            }

            if (isChecked) {
                if (fileNames.add(name)) {
                    imageSelectedCount++;
                }
            } else {
                if (fileNames.remove(name)) {
                    imageSelectedCount--;
                }
            }

            setChecked(position, id, isChecked);
            if (imageSelectedCount == 1 && maxImages == 1) {
                finishActivity();
                return;
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (maxImages == 1) {
            return false;
        }
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_picker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.done:
                finishActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
