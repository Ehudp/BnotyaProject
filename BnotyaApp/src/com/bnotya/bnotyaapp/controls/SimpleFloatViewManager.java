package com.bnotya.bnotyaapp.controls;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Color;
import android.widget.ListView;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;

public class SimpleFloatViewManager implements DragSortListView.FloatViewManager {

    private Bitmap _floatBitmap;

    private ImageView _imageView;

    private int _floatBGColor = Color.BLACK;

    private ListView _listView;

    public SimpleFloatViewManager(ListView lv) {
        _listView = lv;
    }

    public void setBackgroundColor(int color) {
        _floatBGColor = color;
    }

    /**
     * This simple implementation creates a Bitmap copy of the
     * list item currently shown at ListView <code>position</code>.
     */
    @Override
    public View onCreateFloatView(int position) {
        // Guaranteed that this will not be null? I think so. Nope, got
        // a NullPointerException once...
        View v = _listView.getChildAt(position + _listView.getHeaderViewsCount() - _listView.getFirstVisiblePosition());

        if (v == null) {
            return null;
        }

        v.setPressed(false);

        // Create a copy of the drawing cache so that it does not get
        // recycled by the framework when the list tries to clean up memory
        //v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        v.setDrawingCacheEnabled(true);
        _floatBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        if (_imageView == null) {
            _imageView = new ImageView(_listView.getContext());
        }
        _imageView.setBackgroundColor(_floatBGColor);
        _imageView.setPadding(0, 0, 0, 0);
        _imageView.setImageBitmap(_floatBitmap);
        _imageView.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));

        return _imageView;
    }

    /**
     * This does nothing
     */
    @Override
    public void onDragFloatView(View floatView, Point position, Point touch) {
        // do nothing
    }

    /**
     * Removes the Bitmap from the ImageView created in
     * onCreateFloatView() and tells the system to recycle it.
     */
    @Override
    public void onDestroyFloatView(View floatView) {
        ((ImageView) floatView).setImageDrawable(null);

        _floatBitmap.recycle();
        _floatBitmap = null;
    }
}
