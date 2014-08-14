package com.bnotya.bnotyaapp.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bnotya.bnotyaapp.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DragSortListView extends ListView
{
    /**
     * The View that floats above the ListView and represents
     * the dragged item.
     */
    private View _floatView;

    /**
     * The float View location. First based on touch location
     * and given deltaX and deltaY. Then restricted by callback
     * to FloatViewManager.onDragFloatView(). Finally restricted
     * by bounds of DSLV.
     */
    private Point _floatLoc = new Point();

    private Point _touchLoc = new Point();

    /**
     * The middle (in the y-direction) of the floating View.
     */
    private int _floatViewMid;

    /**
     * Flag to make sure float View isn't measured twice
     */
    private boolean _floatViewOnMeasured = false;

    /**
     * Watch the Adapter for data changes. Cancel a drag if
     * coincident with a change.
     */
    private DataSetObserver _observer;

    /**
     * Transparency for the floating View (XML attribute).
     */
    private float _floatAlpha = 1.0f;
    private float _currFloatAlpha = 1.0f;

    /**
     * While drag-sorting, the current position of the floating
     * View. If dropped, the dragged item will land in this position.
     */
    private int _floatPos;

    /**
     * The first expanded ListView position that helps represent
     * the drop slot tracking the floating View.
     */
    private int _firstExpPos;

    /**
     * The second expanded ListView position that helps represent
     * the drop slot tracking the floating View. This can equal
     * mFirstExpPos if there is no slide shuffle occurring; otherwise
     * it is equal to mFirstExpPos + 1.
     */
    private int _secondExpPos;

    /**
     * Flag set if slide shuffling is enabled.
     */
    private boolean _animate = false;

    /**
     * The user dragged from this position.
     */
    private int _srcPos;

    /**
     * Offset (in x) within the dragged item at which the user
     * picked it up (or first touched down with the digitalis).
     */
    private int _dragDeltaX;

    /**
     * Offset (in y) within the dragged item at which the user
     * picked it up (or first touched down with the digitalis).
     */
    private int _dragDeltaY;


    /**
     * The difference (in x) between screen coordinates and coordinates
     * in this view.
     */
    private int _offsetX;

    /**
     * The difference (in y) between screen coordinates and coordinates
     * in this view.
     */
    private int _offsetY;

    /**
     * A listener that receives callbacks whenever the floating View
     * hovers over a new position.
     */
    private DragListener _dragListener;

    /**
     * A listener that receives a callback when the floating View
     * is dropped.
     */
    private DropListener _dropListener;

    /**
     * A listener that receives a callback when the floating View
     * (or more precisely the originally dragged item) is removed
     * by one of the provided gestures.
     */
    private RemoveListener _removeListener;

    /**
     * Enable/Disable item dragging
     *
     * @attr name dslv:drag_enabled
     */
    private boolean _dragEnabled = true;

    /**
     * Drag state enum.
     */
    private final static int IDLE = 0;
    private final static int REMOVING = 1;
    private final static int DROPPING = 2;
    private final static int STOPPED = 3;
    private final static int DRAGGING = 4;

    private int _dragState = IDLE;

    /**
     * Height in pixels to which the originally dragged item
     * is collapsed during a drag-sort. Currently, this value
     * must be greater than zero.
     */
    private int _itemHeightCollapsed = 1;

    /**
     * Height of the floating View. Stored for the purpose of
     * providing the tracking drop slot.
     */
    private int _floatViewHeight;

    /**
     * Convenience member. See above.
     */
    private int _floatViewHeightHalf;

    /**
     * Save the given width spec for use in measuring children
     */
    private int _widthMeasureSpec = 0;

    /**
     * Sample Views ultimately used for calculating the height
     * of ListView items that are off-screen.
     */
    private View[] _sampleViewTypes = new View[1];

    /**
     * Drag-scroll encapsulator!
     */
    private DragScroller _dragScroller;

    /**
     * Determines the start of the upward drag-scroll region
     * at the top of the ListView. Specified by a fraction
     * of the ListView height, thus screen resolution agnostic.
     */
    private float _dragUpScrollStartFrac = 1.0f / 3.0f;

    /**
     * Determines the start of the downward drag-scroll region
     * at the bottom of the ListView. Specified by a fraction
     * of the ListView height, thus screen resolution agnostic.
     */
    private float _dragDownScrollStartFrac = 1.0f / 3.0f;

    /**
     * The following are calculated from the above fracs.
     */
    private int _upScrollStartY;
    private int _downScrollStartY;
    private float _downScrollStartYF;
    private float _upScrollStartYF;

    /**
     * Calculated from above above and current ListView height.
     */
    private float _dragUpScrollHeight;

    /**
     * Calculated from above above and current ListView height.
     */
    private float _dragDownScrollHeight;

    /**
     * Maximum drag-scroll speed in pixels per ms. Only used with
     * default linear drag-scroll profile.
     */
    private float _maxScrollSpeed = 0.5f;

    /**
     * Defines the scroll speed during a drag-scroll. User can
     * provide their own; this default is a simple linear profile
     * where scroll speed increases linearly as the floating View
     * nears the top/bottom of the ListView.
     */
    private DragScrollProfile _scrollProfile = new DragScrollProfile()
    {
        @Override
        public float getSpeed(float w, long t)
        {
            return _maxScrollSpeed * w;
        }
    };

    /**
     * Current touch x.
     */
    private int _x;

    /**
     * Current touch y.
     */
    private int _y;

    /**
     * Last touch x.
     */
    private int _lastX;

    /**
     * Last touch y.
     */
    private int _lastY;

    /**
     * The touch y-coord at which drag started
     */
    private int _dragStartY;

    /**
     * Drag flag bit. Floating View can move in the positive
     * x direction.
     */
    public final static int DRAG_POS_X = 0x1;

    /**
     * Drag flag bit. Floating View can move in the negative
     * x direction.
     */
    public final static int DRAG_NEG_X = 0x2;

    /**
     * Drag flag bit. Floating View can move in the positive
     * y direction. This is subtle. What this actually means is
     * that, if enabled, the floating View can be dragged below its starting
     * position. Remove in favor of upper-bounding item position?
     */
    public final static int DRAG_POS_Y = 0x4;

    /**
     * Drag flag bit. Floating View can move in the negative
     * y direction. This is subtle. What this actually means is
     * that the floating View can be dragged above its starting
     * position. Remove in favor of lower-bounding item position?
     */
    public final static int DRAG_NEG_Y = 0x8;

    /**
     * Flags that determine limits on the motion of the
     * floating View. See flags above.
     */
    private int _dragFlags = 0;

    /**
     * Last call to an on*TouchEvent was a call to
     * onInterceptTouchEvent.
     */
    private boolean _lastCallWasIntercept = false;

    /**
     * A touch event is in progress.
     */
    private boolean _inTouchEvent = false;

    /**
     * Let the user customize the floating View.
     */
    private FloatViewManager _floatViewManager = null;

    /**
     * Given to ListView to cancel its action when a drag-sort
     * begins.
     */
    private MotionEvent _cancelEvent;

    /**
     * Enum telling where to cancel the ListView action when a
     * drag-sort begins
     */
    private static final int NO_CANCEL = 0;
    private static final int ON_TOUCH_EVENT = 1;
    private static final int ON_INTERCEPT_TOUCH_EVENT = 2;

    /**
     * Where to cancel the ListView action when a
     * drag-sort begins
     */
    private int _cancelMethod = NO_CANCEL;

    /**
     * Determines when a slide shuffle animation starts. That is,
     * defines how close to the edge of the drop slot the floating
     * View must be to initiate the slide.
     */
    private float _slideRegionFrac = 0.25f;

    /**
     * Number between 0 and 1 indicating the relative location of
     * a sliding item (only used if drag-sort animations
     * are turned on). Nearly 1 means the item is
     * at the top of the slide region (nearly full blank item
     * is directly below).
     */
    private float _slideFrac = 0.0f;

    /**
     * Wraps the user-provided ListAdapter. This is used to wrap each
     * item View given by the user inside another View (currenly
     * a RelativeLayout) which
     * expands and collapses to simulate the item shuffling.
     */
    private AdapterWrapper _adapterWrapper;

    /**
     * Turn on custom debugger.
     */
    private boolean _trackDragSort = false;

    /**
     * Debugging class.
     */
    private DragSortTracker _dragSortTracker;

    /**
     * Needed for adjusting item heights from within layoutChildren
     */
    private boolean _blockLayoutRequests = false;

    /**
     * Set to true when a down event happens during drag sort;
     * for example, when drag finish animations are
     * playing.
     */
    private boolean _ignoreTouchEvent = false;

    /**
     * Caches DragSortItemView child heights. Sometimes DSLV has to
     * know the height of an offscreen item. Since ListView virtualizes
     * these, DSLV must get the item from the ListAdapter to obtain
     * its height. That process can be expensive, but often the same
     * offscreen item will be requested many times in a row. Once an
     * offscreen item height is calculated, we cache it in this guy.
     * Actually, we cache the height of the child of the
     * DragSortItemView since the item height changes often during a
     * drag-sort.
     */
    private static final int _cacheSize = 3;
    private HeightCache _childHeightCache = new HeightCache(_cacheSize);

    private RemoveAnimator _removeAnimator;

    private LiftAnimator _liftAnimator;

    private DropAnimator _dropAnimator;

    private boolean _useRemoveVelocity;
    private float _removeVelocityX = 0;

    public DragSortListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        int defaultDuration = 150;
        int removeAnimDuration = defaultDuration; // ms
        int dropAnimDuration = defaultDuration; // ms

        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.DragSortListView, 0, 0);

            _itemHeightCollapsed = Math.max(1, a.getDimensionPixelSize(
                    R.styleable.DragSortListView_collapsed_height, 1));

            _trackDragSort = a.getBoolean(
                    R.styleable.DragSortListView_track_drag_sort, false);

            if (_trackDragSort)
            {
                _dragSortTracker = new DragSortTracker();
            }

            // alpha between 0 and 255, 0=transparent, 255=opaque
            _floatAlpha = a.getFloat(R.styleable.DragSortListView_float_alpha, _floatAlpha);
            _currFloatAlpha = _floatAlpha;

            _dragEnabled = a.getBoolean(R.styleable.DragSortListView_drag_enabled, _dragEnabled);

            _slideRegionFrac = Math.max(0.0f,
                    Math.min(1.0f, 1.0f - a.getFloat(
                            R.styleable.DragSortListView_slide_shuffle_speed,
                            0.75f))
            );

            _animate = _slideRegionFrac > 0.0f;

            float frac = a.getFloat(
                    R.styleable.DragSortListView_drag_scroll_start,
                    _dragUpScrollStartFrac);

            setDragScrollStart(frac);

            _maxScrollSpeed = a.getFloat(
                    R.styleable.DragSortListView_max_drag_scroll_speed,
                    _maxScrollSpeed);

            removeAnimDuration = a.getInt(
                    R.styleable.DragSortListView_remove_animation_duration,
                    removeAnimDuration);

            dropAnimDuration = a.getInt(
                    R.styleable.DragSortListView_drop_animation_duration,
                    dropAnimDuration);

            boolean useDefault = a.getBoolean(
                    R.styleable.DragSortListView_use_default_controller,
                    true);

            if (useDefault)
            {
                boolean removeEnabled = a.getBoolean(
                        R.styleable.DragSortListView_remove_enabled,
                        false);
                int removeMode = a.getInt(
                        R.styleable.DragSortListView_remove_mode,
                        DragSortController.FLING_REMOVE);
                boolean sortEnabled = a.getBoolean(
                        R.styleable.DragSortListView_sort_enabled,
                        true);
                int dragInitMode = a.getInt(
                        R.styleable.DragSortListView_drag_start_mode,
                        DragSortController.ON_DOWN);
                int dragHandleId = a.getResourceId(
                        R.styleable.DragSortListView_drag_handle_id,
                        0);
                int flingHandleId = a.getResourceId(
                        R.styleable.DragSortListView_fling_handle_id,
                        0);
                int clickRemoveId = a.getResourceId(
                        R.styleable.DragSortListView_click_remove_id,
                        0);
                int bgColor = a.getColor(
                        R.styleable.DragSortListView_float_background_color,
                        Color.BLACK);

                DragSortController controller = new DragSortController(
                        this, dragHandleId, dragInitMode, removeMode,
                        clickRemoveId, flingHandleId);
                controller.setRemoveEnabled(removeEnabled);
                controller.setSortEnabled(sortEnabled);
                controller.setBackgroundColor(bgColor);

                _floatViewManager = controller;
                setOnTouchListener(controller);
            }

            a.recycle();
        }

        _dragScroller = new DragScroller();

        float smoothness = 0.5f;
        if (removeAnimDuration > 0)
        {
            _removeAnimator = new RemoveAnimator(smoothness, removeAnimDuration);
        }

        if (dropAnimDuration > 0)
        {
            _dropAnimator = new DropAnimator(smoothness, dropAnimDuration);
        }

        _cancelEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0f, 0f, 0, 0f,
                0f, 0, 0);

        // construct the dataset observer
        _observer = new DataSetObserver()
        {
            private void cancel()
            {
                if (_dragState == DRAGGING)
                {
                    cancelDrag();
                }
            }

            @Override
            public void onChanged()
            {
                cancel();
            }

            @Override
            public void onInvalidated()
            {
                cancel();
            }
        };
    }

    /**
     * Usually called from a FloatViewManager. The float alpha
     * will be reset to the xml-defined value every time a drag
     * is stopped.
     */
    public void setFloatAlpha(float alpha)
    {
        _currFloatAlpha = alpha;
    }

    public float getFloatAlpha()
    {
        return _currFloatAlpha;
    }

    /**
     * Set maximum drag scroll speed in positions/second. Only applies
     * if using default ScrollSpeedProfile.
     *
     * @param max Maximum scroll speed.
     */
    public void setMaxScrollSpeed(float max)
    {
        _maxScrollSpeed = max;
    }

    /**
     * For each DragSortListView Listener interface implemented by
     * <code>adapter</code>, this method calls the appropriate
     * set*Listener method with <code>adapter</code> as the argument.
     *
     * @param adapter The ListAdapter providing data to back
     *                DragSortListView.
     * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
     */
    @Override
    public void setAdapter(ListAdapter adapter)
    {
        if (adapter != null)
        {
            _adapterWrapper = new AdapterWrapper(adapter);
            adapter.registerDataSetObserver(_observer);

            if (adapter instanceof DropListener)
            {
                setDropListener((DropListener) adapter);
            }
            if (adapter instanceof DragListener)
            {
                setDragListener((DragListener) adapter);
            }
            if (adapter instanceof RemoveListener)
            {
                setRemoveListener((RemoveListener) adapter);
            }
        }
        else
        {
            _adapterWrapper = null;
        }

        super.setAdapter(_adapterWrapper);
    }

    /**
     * As opposed to {@link ListView#getAdapter()}, which returns
     * a heavily wrapped ListAdapter (DragSortListView wraps the
     * input ListAdapter {\emph and} ListView wraps the wrapped one).
     *
     * @return The ListAdapter set as the argument of setAdapter()
     */
    public ListAdapter getInputAdapter()
    {
        if (_adapterWrapper == null)
        {
            return null;
        }
        else
        {
            return _adapterWrapper.getAdapter();
        }
    }

    private class AdapterWrapper extends BaseAdapter
    {
        private ListAdapter mAdapter;

        public AdapterWrapper(ListAdapter adapter)
        {
            super();
            mAdapter = adapter;

            mAdapter.registerDataSetObserver(new DataSetObserver()
            {
                public void onChanged()
                {
                    notifyDataSetChanged();
                }

                public void onInvalidated()
                {
                    notifyDataSetInvalidated();
                }
            });
        }

        public ListAdapter getAdapter()
        {
            return mAdapter;
        }

        @Override
        public long getItemId(int position)
        {
            return mAdapter.getItemId(position);
        }

        @Override
        public Object getItem(int position)
        {
            return mAdapter.getItem(position);
        }

        @Override
        public int getCount()
        {
            return mAdapter.getCount();
        }

        @Override
        public boolean areAllItemsEnabled()
        {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position)
        {
            return mAdapter.isEnabled(position);
        }

        @Override
        public int getItemViewType(int position)
        {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount()
        {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public boolean hasStableIds()
        {
            return mAdapter.hasStableIds();
        }

        @Override
        public boolean isEmpty()
        {
            return mAdapter.isEmpty();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            DragSortItemView v;
            View child;
            // Log.d("mobeta",
            // "getView: position="+position+" convertView="+convertView);
            if (convertView != null)
            {
                v = (DragSortItemView) convertView;
                View oldChild = v.getChildAt(0);

                child = mAdapter.getView(position, oldChild, DragSortListView.this);
                if (child != oldChild)
                {
                    // shouldn't get here if user is reusing convertViews
                    // properly
                    if (oldChild != null)
                    {
                        v.removeViewAt(0);
                    }
                    v.addView(child);
                }
            }
            else
            {
                child = mAdapter.getView(position, null, DragSortListView.this);
                if (child instanceof Checkable)
                {
                    v = new DragSortItemViewCheckable(getContext());
                }
                else
                {
                    v = new DragSortItemView(getContext());
                }
                v.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                v.addView(child);
            }

            // Set the correct item height given drag state; passed
            // View needs to be measured if measurement is required.
            adjustItem(position + getHeaderViewsCount(), v, true);

            return v;
        }
    }

    private void drawDivider(int expPosition, Canvas canvas)
    {

        final Drawable divider = getDivider();
        final int dividerHeight = getDividerHeight();
        // Log.d("mobeta", "div="+divider+" divH="+dividerHeight);

        if (divider != null && dividerHeight != 0)
        {
            final ViewGroup expItem = (ViewGroup) getChildAt(expPosition
                    - getFirstVisiblePosition());
            if (expItem != null)
            {
                final int l = getPaddingLeft();
                final int r = getWidth() - getPaddingRight();
                final int t;
                final int b;

                final int childHeight = expItem.getChildAt(0).getHeight();

                if (expPosition > _srcPos)
                {
                    t = expItem.getTop() + childHeight;
                    b = t + dividerHeight;
                }
                else
                {
                    b = expItem.getBottom() - childHeight;
                    t = b - dividerHeight;
                }
                // Log.d("mobeta", "l="+l+" t="+t+" r="+r+" b="+b);

                // Have to clip to support ColorDrawable on <= Gingerbread
                canvas.save();
                canvas.clipRect(l, t, r, b);
                divider.setBounds(l, t, r, b);
                divider.draw(canvas);
                canvas.restore();
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);

        if (_dragState != IDLE)
        {
            // draw the divider over the expanded item
            if (_firstExpPos != _srcPos)
            {
                drawDivider(_firstExpPos, canvas);
            }
            if (_secondExpPos != _firstExpPos && _secondExpPos != _srcPos)
            {
                drawDivider(_secondExpPos, canvas);
            }
        }

        if (_floatView != null)
        {
            // draw the float view over everything
            final int w = _floatView.getWidth();
            final int h = _floatView.getHeight();

            int x = _floatLoc.x;

            int width = getWidth();
            if (x < 0)
                x = -x;
            float alphaMod;
            if (x < width)
            {
                alphaMod = ((float) (width - x)) / ((float) width);
                alphaMod *= alphaMod;
            }
            else
            {
                alphaMod = 0;
            }

            final int alpha = (int) (255f * _currFloatAlpha * alphaMod);

            canvas.save();
            // Log.d("mobeta", "clip rect bounds: " + canvas.getClipBounds());
            canvas.translate(_floatLoc.x, _floatLoc.y);
            canvas.clipRect(0, 0, w, h);

            // Log.d("mobeta", "clip rect bounds: " + canvas.getClipBounds());
            canvas.saveLayerAlpha(0, 0, w, h, alpha, Canvas.ALL_SAVE_FLAG);
            _floatView.draw(canvas);
            canvas.restore();
            canvas.restore();
        }
    }

    private int getItemHeight(int position)
    {
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null)
        {
            // item is onscreen, just get the height of the View
            return v.getHeight();
        }
        else
        {
            // item is offscreen. get child height and calculate
            // item height based on current shuffle state
            return calcItemHeight(position, getChildHeight(position));
        }
    }

    private class HeightCache
    {

        private SparseIntArray mMap;
        private ArrayList<Integer> mOrder;
        private int mMaxSize;

        public HeightCache(int size)
        {
            mMap = new SparseIntArray(size);
            mOrder = new ArrayList<Integer>(size);
            mMaxSize = size;
        }

        /**
         * Add item height at position if doesn't already exist.
         */
        public void add(int position, int height)
        {
            int currHeight = mMap.get(position, -1);
            if (currHeight != height)
            {
                if (currHeight == -1)
                {
                    if (mMap.size() == mMaxSize)
                    {
                        // remove oldest entry
                        mMap.delete(mOrder.remove(0));
                    }
                }
                else
                {
                    // move position to newest slot
                    mOrder.remove((Integer) position);
                }
                mMap.put(position, height);
                mOrder.add(position);
            }
        }

        public int get(int position)
        {
            return mMap.get(position, -1);
        }

        public void clear()
        {
            mMap.clear();
            mOrder.clear();
        }

    }

    /**
     * Get the shuffle edge for item at position when top of
     * item is at y-coord top. Assumes that current item heights
     * are consistent with current float view location and
     * thus expanded positions and slide fraction. i.e. Should not be
     * called between update of expanded positions/slide fraction
     * and layoutChildren.
     *
     * @param position
     * @param top
     * @return Shuffle line between position-1 and position (for
     * the given view of the list; that is, for when top of item at
     * position has y-coord of given `top`). If
     * floating View (treated as horizontal line) is dropped
     * immediately above this line, it lands in position-1. If
     * dropped immediately below this line, it lands in position.
     */
    private int getShuffleEdge(int position, int top)
    {
        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        // shuffle edges are defined between items that can be
        // dragged; there are N-1 of them if there are N draggable
        // items.

        if (position <= numHeaders || (position >= getCount() - numFooters))
        {
            return top;
        }

        int divHeight = getDividerHeight();

        int edge;

        int maxBlankHeight = _floatViewHeight - _itemHeightCollapsed;
        int childHeight = getChildHeight(position);
        int itemHeight = getItemHeight(position);

        // first calculate top of item given that floating View is
        // centered over src position
        int otop = top;
        if (_secondExpPos <= _srcPos)
        {
            // items are expanded on and/or above the source position

            if (position == _secondExpPos && _firstExpPos != _secondExpPos)
            {
                if (position == _srcPos)
                {
                    otop = top + itemHeight - _floatViewHeight;
                }
                else
                {
                    int blankHeight = itemHeight - childHeight;
                    otop = top + blankHeight - maxBlankHeight;
                }
            }
            else if (position > _secondExpPos && position <= _srcPos)
            {
                otop = top - maxBlankHeight;
            }

        }
        else
        {
            // items are expanded on and/or below the source position

            if (position > _srcPos && position <= _firstExpPos)
            {
                otop = top + maxBlankHeight;
            }
            else if (position == _secondExpPos && _firstExpPos != _secondExpPos)
            {
                int blankHeight = itemHeight - childHeight;
                otop = top + blankHeight;
            }
        }

        // otop is set
        if (position <= _srcPos)
        {
            edge = otop + (_floatViewHeight - divHeight - getChildHeight(position - 1)) / 2;
        }
        else
        {
            edge = otop + (childHeight - divHeight - _floatViewHeight) / 2;
        }

        return edge;
    }

    private boolean updatePositions()
    {
        final int first = getFirstVisiblePosition();
        int startPos = _firstExpPos;
        View startView = getChildAt(startPos - first);

        if (startView == null)
        {
            startPos = first + getChildCount() / 2;
            startView = getChildAt(startPos - first);
        }
        int startTop = startView.getTop();

        int itemHeight = startView.getHeight();

        int edge = getShuffleEdge(startPos, startTop);
        int lastEdge = edge;

        int divHeight = getDividerHeight();

        int itemPos = startPos;
        int itemTop = startTop;
        if (_floatViewMid < edge)
        {
            // scanning up for float position
            while (itemPos >= 0)
            {
                itemPos--;
                itemHeight = getItemHeight(itemPos);

                if (itemPos == 0)
                {
                    edge = itemTop - divHeight - itemHeight;
                    break;
                }

                itemTop -= itemHeight + divHeight;
                edge = getShuffleEdge(itemPos, itemTop);

                if (_floatViewMid >= edge)
                {
                    break;
                }

                lastEdge = edge;
            }
        }
        else
        {
            // scanning down for float position
            final int count = getCount();
            while (itemPos < count)
            {
                if (itemPos == count - 1)
                {
                    edge = itemTop + divHeight + itemHeight;
                    break;
                }

                itemTop += divHeight + itemHeight;
                itemHeight = getItemHeight(itemPos + 1);
                edge = getShuffleEdge(itemPos + 1, itemTop);

                // test for hit
                if (_floatViewMid < edge)
                {
                    break;
                }

                lastEdge = edge;
                itemPos++;
            }
        }

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        boolean updated = false;

        int oldFirstExpPos = _firstExpPos;
        int oldSecondExpPos = _secondExpPos;
        float oldSlideFrac = _slideFrac;

        if (_animate)
        {
            int edgeToEdge = Math.abs(edge - lastEdge);

            int edgeTop, edgeBottom;
            if (_floatViewMid < edge)
            {
                edgeBottom = edge;
                edgeTop = lastEdge;
            }
            else
            {
                edgeTop = edge;
                edgeBottom = lastEdge;
            }
            // Log.d("mobeta", "edgeTop="+edgeTop+" edgeBot="+edgeBottom);

            int slideRgnHeight = (int) (0.5f * _slideRegionFrac * edgeToEdge);
            float slideRgnHeightF = (float) slideRgnHeight;
            int slideEdgeTop = edgeTop + slideRgnHeight;
            int slideEdgeBottom = edgeBottom - slideRgnHeight;

            // Three regions
            if (_floatViewMid < slideEdgeTop)
            {
                _firstExpPos = itemPos - 1;
                _secondExpPos = itemPos;
                _slideFrac = 0.5f * ((float) (slideEdgeTop - _floatViewMid)) / slideRgnHeightF;
            }
            else if (_floatViewMid < slideEdgeBottom)
            {
                _firstExpPos = itemPos;
                _secondExpPos = itemPos;
            }
            else
            {
                _firstExpPos = itemPos;
                _secondExpPos = itemPos + 1;
                _slideFrac = 0.5f * (1.0f + ((float) (edgeBottom - _floatViewMid))
                        / slideRgnHeightF);
            }

        }
        else
        {
            _firstExpPos = itemPos;
            _secondExpPos = itemPos;
        }

        // correct for headers and footers
        if (_firstExpPos < numHeaders)
        {
            itemPos = numHeaders;
            _firstExpPos = itemPos;
            _secondExpPos = itemPos;
        }
        else if (_secondExpPos >= getCount() - numFooters)
        {
            itemPos = getCount() - numFooters - 1;
            _firstExpPos = itemPos;
            _secondExpPos = itemPos;
        }

        if (_firstExpPos != oldFirstExpPos || _secondExpPos != oldSecondExpPos
                || _slideFrac != oldSlideFrac)
        {
            updated = true;
        }

        if (itemPos != _floatPos)
        {
            if (_dragListener != null)
            {
                _dragListener.drag(_floatPos - numHeaders, itemPos - numHeaders);
            }

            _floatPos = itemPos;
            updated = true;
        }

        return updated;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (_trackDragSort)
        {
            _dragSortTracker.appendState();
        }
    }

    private class SmoothAnimator implements Runnable
    {
        protected long mStartTime;

        private float mDurationF;

        private float mAlpha;
        private float mA, mB, mC, mD;

        private boolean mCanceled;

        public SmoothAnimator(float smoothness, int duration)
        {
            mAlpha = smoothness;
            mDurationF = (float) duration;
            mA = mD = 1f / (2f * mAlpha * (1f - mAlpha));
            mB = mAlpha / (2f * (mAlpha - 1f));
            mC = 1f / (1f - mAlpha);
        }

        public float transform(float frac)
        {
            if (frac < mAlpha)
            {
                return mA * frac * frac;
            }
            else if (frac < 1f - mAlpha)
            {
                return mB + mC * frac;
            }
            else
            {
                return 1f - mD * (frac - 1f) * (frac - 1f);
            }
        }

        public void start()
        {
            mStartTime = SystemClock.uptimeMillis();
            mCanceled = false;
            onStart();
            post(this);
        }

        public void cancel()
        {
            mCanceled = true;
        }

        public void onStart()
        {
            // stub
        }

        public void onUpdate(float frac, float smoothFrac)
        {
            // stub
        }

        public void onStop()
        {
            // stub
        }

        @Override
        public void run()
        {
            if (mCanceled)
            {
                return;
            }

            float fraction = ((float) (SystemClock.uptimeMillis() - mStartTime)) / mDurationF;

            if (fraction >= 1f)
            {
                onUpdate(1f, 1f);
                onStop();
            }
            else
            {
                onUpdate(fraction, transform(fraction));
                post(this);
            }
        }
    }

    /**
     * Centers floating View under touch point.
     */
    private class LiftAnimator extends SmoothAnimator
    {

        private float mInitDragDeltaY;
        private float mFinalDragDeltaY;

        public LiftAnimator(float smoothness, int duration)
        {
            super(smoothness, duration);
        }

        @Override
        public void onStart()
        {
            mInitDragDeltaY = _dragDeltaY;
            mFinalDragDeltaY = _floatViewHeightHalf;
        }

        @Override
        public void onUpdate(float frac, float smoothFrac)
        {
            if (_dragState != DRAGGING)
            {
                cancel();
            }
            else
            {
                _dragDeltaY = (int) (smoothFrac * mFinalDragDeltaY + (1f - smoothFrac)
                        * mInitDragDeltaY);
                _floatLoc.y = _y - _dragDeltaY;
                doDragFloatView(true);
            }
        }
    }

    /**
     * Centers floating View over drop slot before destroying.
     */
    private class DropAnimator extends SmoothAnimator
    {

        private int mDropPos;
        private int srcPos;
        private float mInitDeltaY;
        private float mInitDeltaX;

        public DropAnimator(float smoothness, int duration)
        {
            super(smoothness, duration);
        }

        @Override
        public void onStart()
        {
            mDropPos = _floatPos;
            srcPos = _srcPos;
            _dragState = DROPPING;
            mInitDeltaY = _floatLoc.y - getTargetY();
            mInitDeltaX = _floatLoc.x - getPaddingLeft();
        }

        private int getTargetY()
        {
            final int first = getFirstVisiblePosition();
            final int otherAdjust = (_itemHeightCollapsed + getDividerHeight()) / 2;
            View v = getChildAt(mDropPos - first);
            int targetY = -1;
            if (v != null)
            {
                if (mDropPos == srcPos)
                {
                    targetY = v.getTop();
                }
                else if (mDropPos < srcPos)
                {
                    // expanded down
                    targetY = v.getTop() - otherAdjust;
                }
                else
                {
                    // expanded up
                    targetY = v.getBottom() + otherAdjust - _floatViewHeight;
                }
            }
            else
            {
                // drop position is not on screen?? no animation
                cancel();
            }

            return targetY;
        }

        @Override
        public void onUpdate(float frac, float smoothFrac)
        {
            final int targetY = getTargetY();
            final int targetX = getPaddingLeft();
            final float deltaY = _floatLoc.y - targetY;
            final float deltaX = _floatLoc.x - targetX;
            final float f = 1f - smoothFrac;
            if (f < Math.abs(deltaY / mInitDeltaY) || f < Math.abs(deltaX / mInitDeltaX))
            {
                _floatLoc.y = targetY + (int) (mInitDeltaY * f);
                _floatLoc.x = getPaddingLeft() + (int) (mInitDeltaX * f);
                doDragFloatView(true);
            }
        }

        @Override
        public void onStop()
        {
            dropFloatView();
        }

    }

    /**
     * Collapses expanded items.
     */
    private class RemoveAnimator extends SmoothAnimator
    {

        private float mFloatLocX;
        private float mFirstStartBlank;
        private float mSecondStartBlank;

        private int mFirstChildHeight = -1;
        private int mSecondChildHeight = -1;

        private int mFirstPos;
        private int mSecondPos;
        private int srcPos;

        public RemoveAnimator(float smoothness, int duration)
        {
            super(smoothness, duration);
        }

        @Override
        public void onStart()
        {
            mFirstChildHeight = -1;
            mSecondChildHeight = -1;
            mFirstPos = _firstExpPos;
            mSecondPos = _secondExpPos;
            srcPos = _srcPos;
            _dragState = REMOVING;

            mFloatLocX = _floatLoc.x;
            if (_useRemoveVelocity)
            {
                float minVelocity = 2f * getWidth();
                if (_removeVelocityX == 0)
                {
                    _removeVelocityX = (mFloatLocX < 0 ? -1 : 1) * minVelocity;
                }
                else
                {
                    minVelocity *= 2;
                    if (_removeVelocityX < 0 && _removeVelocityX > -minVelocity)
                        _removeVelocityX = -minVelocity;
                    else if (_removeVelocityX > 0 && _removeVelocityX < minVelocity)
                        _removeVelocityX = minVelocity;
                }
            }
            else
            {
                destroyFloatView();
            }
        }

        @Override
        public void onUpdate(float frac, float smoothFrac)
        {
            float f = 1f - smoothFrac;

            final int firstVis = getFirstVisiblePosition();
            View item = getChildAt(mFirstPos - firstVis);
            ViewGroup.LayoutParams lp;
            int blank;

            if (_useRemoveVelocity)
            {
                float dt = (float) (SystemClock.uptimeMillis() - mStartTime) / 1000;
                if (dt == 0)
                    return;
                float dx = _removeVelocityX * dt;
                int w = getWidth();
                _removeVelocityX += (_removeVelocityX > 0 ? 1 : -1) * dt * w;
                mFloatLocX += dx;
                _floatLoc.x = (int) mFloatLocX;
                if (mFloatLocX < w && mFloatLocX > -w)
                {
                    mStartTime = SystemClock.uptimeMillis();
                    doDragFloatView(true);
                    return;
                }
            }

            if (item != null)
            {
                if (mFirstChildHeight == -1)
                {
                    mFirstChildHeight = getChildHeight(mFirstPos, item, false);
                    mFirstStartBlank = (float) (item.getHeight() - mFirstChildHeight);
                }
                blank = Math.max((int) (f * mFirstStartBlank), 1);
                lp = item.getLayoutParams();
                lp.height = mFirstChildHeight + blank;
                item.setLayoutParams(lp);
            }
            if (mSecondPos != mFirstPos)
            {
                item = getChildAt(mSecondPos - firstVis);
                if (item != null)
                {
                    if (mSecondChildHeight == -1)
                    {
                        mSecondChildHeight = getChildHeight(mSecondPos, item, false);
                        mSecondStartBlank = (float) (item.getHeight() - mSecondChildHeight);
                    }
                    blank = Math.max((int) (f * mSecondStartBlank), 1);
                    lp = item.getLayoutParams();
                    lp.height = mSecondChildHeight + blank;
                    item.setLayoutParams(lp);
                }
            }
        }

        @Override
        public void onStop()
        {
            doRemoveItem();
        }
    }

    public void removeItem(int which)
    {

        _useRemoveVelocity = false;
        removeItem(which, 0);
    }

    /**
     * Removes an item from the list and animates the removal.
     *
     * @param which     Position to remove (NOTE: headers/footers ignored!
     *                  this is a position in your input ListAdapter).
     * @param velocityX
     */
    public void removeItem(int which, float velocityX)
    {
        if (_dragState == IDLE || _dragState == DRAGGING)
        {

            if (_dragState == IDLE)
            {
                // called from outside drag-sort
                _srcPos = getHeaderViewsCount() + which;
                _firstExpPos = _srcPos;
                _secondExpPos = _srcPos;
                _floatPos = _srcPos;
                View v = getChildAt(_srcPos - getFirstVisiblePosition());
                if (v != null)
                {
                    v.setVisibility(View.INVISIBLE);
                }
            }

            _dragState = REMOVING;
            _removeVelocityX = velocityX;

            if (_inTouchEvent)
            {
                switch (_cancelMethod)
                {
                    case ON_TOUCH_EVENT:
                        super.onTouchEvent(_cancelEvent);
                        break;
                    case ON_INTERCEPT_TOUCH_EVENT:
                        super.onInterceptTouchEvent(_cancelEvent);
                        break;
                }
            }

            if (_removeAnimator != null)
            {
                _removeAnimator.start();
            }
            else
            {
                doRemoveItem(which);
            }
        }
    }

    /**
     * Move an item, bypassing the drag-sort process. Simply calls
     * through to {@link DropListener#drop(int, int)}.
     *
     * @param from Position to move (NOTE: headers/footers ignored!
     *             this is a position in your input ListAdapter).
     * @param to   Target position (NOTE: headers/footers ignored!
     *             this is a position in your input ListAdapter).
     */
    public void moveItem(int from, int to)
    {
        if (_dropListener != null)
        {
            final int count = getInputAdapter().getCount();
            if (from >= 0 && from < count && to >= 0 && to < count)
            {
                _dropListener.drop(from, to);
            }
        }
    }

    /**
     * Cancel a drag. Calls stopDrag(boolean, boolean) with
     * <code>true</code> as the first argument.
     */
    public void cancelDrag()
    {
        if (_dragState == DRAGGING)
        {
            _dragScroller.stopScrolling(true);
            destroyFloatView();
            clearPositions();
            adjustAllItems();

            if (_inTouchEvent)
            {
                _dragState = STOPPED;
            }
            else
            {
                _dragState = IDLE;
            }
        }
    }

    private void clearPositions()
    {
        _srcPos = -1;
        _firstExpPos = -1;
        _secondExpPos = -1;
        _floatPos = -1;
    }

    private void dropFloatView()
    {
        // must set to avoid cancelDrag being called from the
        // DataSetObserver
        _dragState = DROPPING;

        if (_dropListener != null && _floatPos >= 0 && _floatPos < getCount())
        {
            final int numHeaders = getHeaderViewsCount();
            _dropListener.drop(_srcPos - numHeaders, _floatPos - numHeaders);
        }

        destroyFloatView();

        adjustOnReorder();
        clearPositions();
        adjustAllItems();

        // now the drag is done
        if (_inTouchEvent)
        {
            _dragState = STOPPED;
        }
        else
        {
            _dragState = IDLE;
        }
    }

    private void doRemoveItem()
    {
        doRemoveItem(_srcPos - getHeaderViewsCount());
    }

    /**
     * Removes dragged item from the list. Calls RemoveListener.
     */
    private void doRemoveItem(int which)
    {
        // must set to avoid cancelDrag being called from the
        // DataSetObserver
        _dragState = REMOVING;

        // end it
        if (_removeListener != null)
        {
            _removeListener.remove(which);
        }

        destroyFloatView();

        adjustOnReorder();
        clearPositions();

        // now the drag is done
        if (_inTouchEvent)
        {
            _dragState = STOPPED;
        }
        else
        {
            _dragState = IDLE;
        }
    }

    private void adjustOnReorder()
    {
        final int firstPos = getFirstVisiblePosition();

        if (_srcPos < firstPos)
        {
            // collapsed src item is off screen;
            // adjust the scroll after item heights have been fixed
            View v = getChildAt(0);
            int top = 0;
            if (v != null)
            {
                top = v.getTop();
            }

            setSelectionFromTop(firstPos - 1, top - getPaddingTop());
        }
    }

    /**
     * Stop a drag in progress. Pass <code>true</code> if you would
     * like to remove the dragged item from the list.
     *
     * @param remove Remove the dragged item from the list. Calls
     *               a registered RemoveListener, if one exists. Otherwise, calls
     *               the DropListener, if one exists.
     * @return True if the stop was successful. False if there is
     * no floating View.
     */
    public boolean stopDrag(boolean remove)
    {
        _useRemoveVelocity = false;
        return stopDrag(remove, 0);
    }

    public boolean stopDragWithVelocity(boolean remove, float velocityX)
    {

        _useRemoveVelocity = true;
        return stopDrag(remove, velocityX);
    }

    public boolean stopDrag(boolean remove, float velocityX)
    {
        if (_floatView != null)
        {
            _dragScroller.stopScrolling(true);

            if (remove)
            {
                removeItem(_srcPos - getHeaderViewsCount(), velocityX);
            }
            else
            {
                if (_dropAnimator != null)
                {
                    _dropAnimator.start();
                }
                else
                {
                    dropFloatView();
                }
            }

            if (_trackDragSort)
            {
                _dragSortTracker.stopTracking();
            }

            return true;
        }
        else
        {
            // stop failed
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (_ignoreTouchEvent)
        {
            _ignoreTouchEvent = false;
            return false;
        }

        if (!_dragEnabled)
        {
            return super.onTouchEvent(ev);
        }

        boolean more = false;

        boolean lastCallWasIntercept = _lastCallWasIntercept;
        _lastCallWasIntercept = false;

        if (!lastCallWasIntercept)
        {
            saveTouchCoords(ev);
        }

        if (_dragState == DRAGGING)
        {
            onDragTouchEvent(ev);
            more = true; // give us more!
        }
        else
        {
            // what if float view is null b/c we dropped in middle
            // of drag touch event?

            if (_dragState == IDLE)
            {
                if (super.onTouchEvent(ev))
                {
                    more = true;
                }
            }

            int action = ev.getAction() & MotionEvent.ACTION_MASK;

            switch (action)
            {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (more)
                    {
                        _cancelMethod = ON_TOUCH_EVENT;
                    }
            }
        }

        return more;
    }

    private void doActionUpOrCancel()
    {
        _cancelMethod = NO_CANCEL;
        _inTouchEvent = false;
        if (_dragState == STOPPED)
        {
            _dragState = IDLE;
        }
        _currFloatAlpha = _floatAlpha;
        mListViewIntercepted = false;
        _childHeightCache.clear();
    }

    private void saveTouchCoords(MotionEvent ev)
    {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_DOWN)
        {
            _lastX = _x;
            _lastY = _y;
        }
        _x = (int) ev.getX();
        _y = (int) ev.getY();
        if (action == MotionEvent.ACTION_DOWN)
        {
            _lastX = _x;
            _lastY = _y;
        }
        _offsetX = (int) ev.getRawX() - _x;
        _offsetY = (int) ev.getRawY() - _y;
    }

    public boolean listViewIntercepted()
    {
        return mListViewIntercepted;
    }

    private boolean mListViewIntercepted = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (!_dragEnabled)
        {
            return super.onInterceptTouchEvent(ev);
        }

        saveTouchCoords(ev);
        _lastCallWasIntercept = true;

        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN)
        {
            if (_dragState != IDLE)
            {
                // intercept and ignore
                _ignoreTouchEvent = true;
                return true;
            }
            _inTouchEvent = true;
        }

        boolean intercept = false;

        // the following deals with calls to super.onInterceptTouchEvent
        if (_floatView != null)
        {
            // super's touch event canceled in startDrag
            intercept = true;
        }
        else
        {
            if (super.onInterceptTouchEvent(ev))
            {
                mListViewIntercepted = true;
                intercept = true;
            }

            switch (action)
            {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (intercept)
                    {
                        _cancelMethod = ON_TOUCH_EVENT;
                    }
                    else
                    {
                        _cancelMethod = ON_INTERCEPT_TOUCH_EVENT;
                    }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            _inTouchEvent = false;
        }

        return intercept;
    }

    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param heightFraction Fraction of ListView height. Capped at
     *                       0.5f.
     */
    public void setDragScrollStart(float heightFraction)
    {
        setDragScrollStarts(heightFraction, heightFraction);
    }

    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param upperFrac Fraction of ListView height for up-scroll bound.
     *                  Capped at 0.5f.
     * @param lowerFrac Fraction of ListView height for down-scroll bound.
     *                  Capped at 0.5f.
     */
    public void setDragScrollStarts(float upperFrac, float lowerFrac)
    {
        if (lowerFrac > 0.5f)
        {
            _dragDownScrollStartFrac = 0.5f;
        }
        else
        {
            _dragDownScrollStartFrac = lowerFrac;
        }

        if (upperFrac > 0.5f)
        {
            _dragUpScrollStartFrac = 0.5f;
        }
        else
        {
            _dragUpScrollStartFrac = upperFrac;
        }

        if (getHeight() != 0)
        {
            updateScrollStarts();
        }
    }

    private void continueDrag(int x, int y)
    {

        // proposed position
        _floatLoc.x = x - _dragDeltaX;
        _floatLoc.y = y - _dragDeltaY;

        doDragFloatView(true);

        int minY = Math.min(y, _floatViewMid + _floatViewHeightHalf);
        int maxY = Math.max(y, _floatViewMid - _floatViewHeightHalf);

        // get the current scroll direction
        int currentScrollDir = _dragScroller.getScrollDir();

        if (minY > _lastY && minY > _downScrollStartY && currentScrollDir != DragScroller.DOWN)
        {
            // dragged down, it is below the down scroll start and it is not
            // scrolling up

            if (currentScrollDir != DragScroller.STOP)
            {
                // moved directly from up scroll to down scroll
                _dragScroller.stopScrolling(true);
            }

            // start scrolling down
            _dragScroller.startScrolling(DragScroller.DOWN);
        }
        else if (maxY < _lastY && maxY < _upScrollStartY && currentScrollDir != DragScroller.UP)
        {
            // dragged up, it is above the up scroll start and it is not
            // scrolling up

            if (currentScrollDir != DragScroller.STOP)
            {
                // moved directly from down scroll to up scroll
                _dragScroller.stopScrolling(true);
            }

            // start scrolling up
            _dragScroller.startScrolling(DragScroller.UP);
        }
        else if (maxY >= _upScrollStartY && minY <= _downScrollStartY
                && _dragScroller.isScrolling())
        {
            // not in the upper nor in the lower drag-scroll regions but it is
            // still scrolling

            _dragScroller.stopScrolling(true);
        }
    }

    private void updateScrollStarts()
    {
        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        float heightF = (float) listHeight;

        _upScrollStartYF = padTop + _dragUpScrollStartFrac * heightF;
        _downScrollStartYF = padTop + (1.0f - _dragDownScrollStartFrac) * heightF;

        _upScrollStartY = (int) _upScrollStartYF;
        _downScrollStartY = (int) _downScrollStartYF;

        _dragUpScrollHeight = _upScrollStartYF - padTop;
        _dragDownScrollHeight = padTop + listHeight - _downScrollStartYF;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollStarts();
    }

    private void adjustAllItems()
    {
        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        int begin = Math.max(0, getHeaderViewsCount() - first);
        int end = Math.min(last - first, getCount() - 1 - getFooterViewsCount() - first);

        for (int i = begin; i <= end; ++i)
        {
            View v = getChildAt(i);
            if (v != null)
            {
                adjustItem(first + i, v, false);
            }
        }
    }

    private void adjustItem(int position)
    {
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null)
        {
            adjustItem(position, v, false);
        }
    }

    /**
     * Sets layout param height, gravity, and visibility  on
     * wrapped item.
     */
    private void adjustItem(int position, View v, boolean invalidChildHeight)
    {

        // Adjust item height
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        int height;
        if (position != _srcPos && position != _firstExpPos && position != _secondExpPos)
        {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else
        {
            height = calcItemHeight(position, v, invalidChildHeight);
        }

        if (height != lp.height)
        {
            lp.height = height;
            v.setLayoutParams(lp);
        }

        // Adjust item gravity
        if (position == _firstExpPos || position == _secondExpPos)
        {
            if (position < _srcPos)
            {
                ((DragSortItemView) v).setGravity(Gravity.BOTTOM);
            }
            else if (position > _srcPos)
            {
                ((DragSortItemView) v).setGravity(Gravity.TOP);
            }
        }

        // Finally adjust item visibility

        int oldVis = v.getVisibility();
        int vis = View.VISIBLE;

        if (position == _srcPos && _floatView != null)
        {
            vis = View.INVISIBLE;
        }

        if (vis != oldVis)
        {
            v.setVisibility(vis);
        }
    }

    private int getChildHeight(int position)
    {
        if (position == _srcPos)
        {
            return 0;
        }

        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null)
        {
            // item is onscreen, therefore child height is valid,
            // hence the "true"
            return getChildHeight(position, v, false);
        }
        else
        {
            // item is offscreen
            // first check cache for child height at this position
            int childHeight = _childHeightCache.get(position);
            if (childHeight != -1)
            {
                // Log.d("mobeta", "found child height in cache!");
                return childHeight;
            }

            final ListAdapter adapter = getAdapter();
            int type = adapter.getItemViewType(position);

            // There might be a better place for checking for the following
            final int typeCount = adapter.getViewTypeCount();
            if (typeCount != _sampleViewTypes.length)
            {
                _sampleViewTypes = new View[typeCount];
            }

            if (type >= 0)
            {
                if (_sampleViewTypes[type] == null)
                {
                    v = adapter.getView(position, null, this);
                    _sampleViewTypes[type] = v;
                }
                else
                {
                    v = adapter.getView(position, _sampleViewTypes[type], this);
                }
            }
            else
            {
                // type is HEADER_OR_FOOTER or IGNORE
                v = adapter.getView(position, null, this);
            }

            // current child height is invalid, hence "true" below
            childHeight = getChildHeight(position, v, true);

            // cache it because this could have been expensive
            _childHeightCache.add(position, childHeight);

            return childHeight;
        }
    }

    private int getChildHeight(int position, View item, boolean invalidChildHeight)
    {
        if (position == _srcPos)
        {
            return 0;
        }

        View child;
        if (position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount())
        {
            child = item;
        }
        else
        {
            child = ((ViewGroup) item).getChildAt(0);
        }

        ViewGroup.LayoutParams lp = child.getLayoutParams();

        if (lp != null)
        {
            if (lp.height > 0)
            {
                return lp.height;
            }
        }

        int childHeight = child.getHeight();

        if (childHeight == 0 || invalidChildHeight)
        {
            measureItem(child);
            childHeight = child.getMeasuredHeight();
        }

        return childHeight;
    }

    private int calcItemHeight(int position, View item, boolean invalidChildHeight)
    {
        return calcItemHeight(position, getChildHeight(position, item, invalidChildHeight));
    }

    private int calcItemHeight(int position, int childHeight)
    {

        int divHeight = getDividerHeight();

        boolean isSliding = _animate && _firstExpPos != _secondExpPos;
        int maxNonSrcBlankHeight = _floatViewHeight - _itemHeightCollapsed;
        int slideHeight = (int) (_slideFrac * maxNonSrcBlankHeight);

        int height;

        if (position == _srcPos)
        {
            if (_srcPos == _firstExpPos)
            {
                if (isSliding)
                {
                    height = slideHeight + _itemHeightCollapsed;
                }
                else
                {
                    height = _floatViewHeight;
                }
            }
            else if (_srcPos == _secondExpPos)
            {
                // if gets here, we know an item is sliding
                height = _floatViewHeight - slideHeight;
            }
            else
            {
                height = _itemHeightCollapsed;
            }
        }
        else if (position == _firstExpPos)
        {
            if (isSliding)
            {
                height = childHeight + slideHeight;
            }
            else
            {
                height = childHeight + maxNonSrcBlankHeight;
            }
        }
        else if (position == _secondExpPos)
        {
            // we know an item is sliding (b/c 2ndPos != 1stPos)
            height = childHeight + maxNonSrcBlankHeight - slideHeight;
        }
        else
        {
            height = childHeight;
        }

        return height;
    }

    @Override
    public void requestLayout()
    {
        if (!_blockLayoutRequests)
        {
            super.requestLayout();
        }
    }

    private int adjustScroll(int movePos, View moveItem, int oldFirstExpPos, int oldSecondExpPos)
    {
        int adjust = 0;

        final int childHeight = getChildHeight(movePos);

        int moveHeightBefore = moveItem.getHeight();
        int moveHeightAfter = calcItemHeight(movePos, childHeight);

        int moveBlankBefore = moveHeightBefore;
        int moveBlankAfter = moveHeightAfter;
        if (movePos != _srcPos)
        {
            moveBlankBefore -= childHeight;
            moveBlankAfter -= childHeight;
        }

        int maxBlank = _floatViewHeight;
        if (_srcPos != _firstExpPos && _srcPos != _secondExpPos)
        {
            maxBlank -= _itemHeightCollapsed;
        }

        if (movePos <= oldFirstExpPos)
        {
            if (movePos > _firstExpPos)
            {
                adjust += maxBlank - moveBlankAfter;
            }
        }
        else if (movePos == oldSecondExpPos)
        {
            if (movePos <= _firstExpPos)
            {
                adjust += moveBlankBefore - maxBlank;
            }
            else if (movePos == _secondExpPos)
            {
                adjust += moveHeightBefore - moveHeightAfter;
            }
            else
            {
                adjust += moveBlankBefore;
            }
        }
        else
        {
            if (movePos <= _firstExpPos)
            {
                adjust -= maxBlank;
            }
            else if (movePos == _secondExpPos)
            {
                adjust -= moveBlankAfter;
            }
        }

        return adjust;
    }

    private void measureItem(View item)
    {
        ViewGroup.LayoutParams lp = item.getLayoutParams();
        if (lp == null)
        {
            lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(lp);
        }
        int wspec = ViewGroup.getChildMeasureSpec(_widthMeasureSpec, getListPaddingLeft()
                + getListPaddingRight(), lp.width);
        int hspec;
        if (lp.height > 0)
        {
            hspec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        }
        else
        {
            hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        item.measure(wspec, hspec);
    }

    private void measureFloatView()
    {
        if (_floatView != null)
        {
            measureItem(_floatView);
            _floatViewHeight = _floatView.getMeasuredHeight();
            _floatViewHeightHalf = _floatViewHeight / 2;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Log.d("mobeta", "onMeasure called");
        if (_floatView != null)
        {
            if (_floatView.isLayoutRequested())
            {
                measureFloatView();
            }
            _floatViewOnMeasured = true; // set to false after layout
        }
        _widthMeasureSpec = widthMeasureSpec;
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();

        if (_floatView != null)
        {
            if (_floatView.isLayoutRequested() && !_floatViewOnMeasured)
            {
                // Have to measure here when usual android measure
                // pass is skipped. This happens during a drag-sort
                // when layoutChildren is called directly.
                measureFloatView();
            }
            _floatView.layout(0, 0, _floatView.getMeasuredWidth(), _floatView.getMeasuredHeight());
            _floatViewOnMeasured = false;
        }
    }

    protected boolean onDragTouchEvent(MotionEvent ev)
    {
        // we are in a drag
        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        switch (ev.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_CANCEL:
                if (_dragState == DRAGGING)
                {
                    cancelDrag();
                }
                doActionUpOrCancel();
                break;
            case MotionEvent.ACTION_UP:
                // Log.d("mobeta", "calling stopDrag from onDragTouchEvent");
                if (_dragState == DRAGGING)
                {
                    stopDrag(false);
                }
                doActionUpOrCancel();
                break;
            case MotionEvent.ACTION_MOVE:
                continueDrag((int) ev.getX(), (int) ev.getY());
                break;
        }

        return true;
    }

    private boolean _floatViewInvalidated = false;

    private void invalidateFloatView()
    {
        _floatViewInvalidated = true;
    }

    /**
     * Start a drag of item at <code>position</code> using the
     * registered FloatViewManager. Calls through
     * to {@link #startDrag(int, View, int, int, int)} after obtaining
     * the floating View from the FloatViewManager.
     *
     * @param position  Item to drag.
     * @param dragFlags Flags that restrict some movements of the
     *                  floating View. For example, set <code>dragFlags |=
     *                  ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     *                  View in all directions except off the screen to the left.
     * @param deltaX    Offset in x of the touch coordinate from the
     *                  left edge of the floating View (i.e. touch-x minus float View
     *                  left).
     * @param deltaY    Offset in y of the touch coordinate from the
     *                  top edge of the floating View (i.e. touch-y minus float View
     *                  top).
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, there is no registered FloatViewManager,
     * or the FloatViewManager returns a null View.
     */
    public boolean startDrag(int position, int dragFlags, int deltaX, int deltaY)
    {
        if (!_inTouchEvent || _floatViewManager == null)
        {
            return false;
        }

        View v = _floatViewManager.onCreateFloatView(position);

        if (v == null)
        {
            return false;
        }
        else
        {
            return startDrag(position, v, dragFlags, deltaX, deltaY);
        }

    }

    /**
     * Start a drag of item at <code>position</code> without using
     * a FloatViewManager.
     *
     * @param position  Item to drag.
     * @param floatView Floating View.
     * @param dragFlags Flags that restrict some movements of the
     *                  floating View. For example, set <code>dragFlags |=
     *                  ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     *                  View in all directions except off the screen to the left.
     * @param deltaX    Offset in x of the touch coordinate from the
     *                  left edge of the floating View (i.e. touch-x minus float View
     *                  left).
     * @param deltaY    Offset in y of the touch coordinate from the
     *                  top edge of the floating View (i.e. touch-y minus float View
     *                  top).
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, <code>floatView</code> is null, or there is
     * a drag in progress.
     */
    public boolean startDrag(int position, View floatView, int dragFlags, int deltaX, int deltaY)
    {
        if (_dragState != IDLE || !_inTouchEvent || _floatView != null || floatView == null
                || !_dragEnabled)
        {
            return false;
        }

        if (getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        int pos = position + getHeaderViewsCount();
        _firstExpPos = pos;
        _secondExpPos = pos;
        _srcPos = pos;
        _floatPos = pos;


        _dragState = DRAGGING;
        _dragFlags = 0;
        _dragFlags |= dragFlags;

        _floatView = floatView;
        measureFloatView();

        _dragDeltaX = deltaX;
        _dragDeltaY = deltaY;
        _dragStartY = _y;

        _floatLoc.x = _x - _dragDeltaX;
        _floatLoc.y = _y - _dragDeltaY;

        // set src item invisible
        final View srcItem = getChildAt(_srcPos - getFirstVisiblePosition());

        if (srcItem != null)
        {
            srcItem.setVisibility(View.INVISIBLE);
        }

        if (_trackDragSort)
        {
            _dragSortTracker.startTracking();
        }

        // once float view is created, events are no longer passed
        // to ListView
        switch (_cancelMethod)
        {
            case ON_TOUCH_EVENT:
                super.onTouchEvent(_cancelEvent);
                break;
            case ON_INTERCEPT_TOUCH_EVENT:
                super.onInterceptTouchEvent(_cancelEvent);
                break;
        }

        requestLayout();

        if (_liftAnimator != null)
        {
            _liftAnimator.start();
        }

        return true;
    }

    private void doDragFloatView(boolean forceInvalidate)
    {
        int movePos = getFirstVisiblePosition() + getChildCount() / 2;
        View moveItem = getChildAt(getChildCount() / 2);

        if (moveItem == null)
        {
            return;
        }

        doDragFloatView(movePos, moveItem, forceInvalidate);
    }

    private void doDragFloatView(int movePos, View moveItem, boolean forceInvalidate)
    {
        _blockLayoutRequests = true;

        updateFloatView();

        int oldFirstExpPos = _firstExpPos;
        int oldSecondExpPos = _secondExpPos;

        boolean updated = updatePositions();

        if (updated)
        {
            adjustAllItems();
            int scroll = adjustScroll(movePos, moveItem, oldFirstExpPos, oldSecondExpPos);
            // Log.d("mobeta", "  adjust scroll="+scroll);

            setSelectionFromTop(movePos, moveItem.getTop() + scroll - getPaddingTop());
            layoutChildren();
        }

        if (updated || forceInvalidate)
        {
            invalidate();
        }

        _blockLayoutRequests = false;
    }

    /**
     * Sets float View location based on suggested values and
     * constraints set in dragFlags.
     */
    private void updateFloatView()
    {

        if (_floatViewManager != null)
        {
            _touchLoc.set(_x, _y);
            _floatViewManager.onDragFloatView(_floatView, _floatLoc, _touchLoc);
        }

        final int floatX = _floatLoc.x;
        final int floatY = _floatLoc.y;

        // restrict x motion
        int padLeft = getPaddingLeft();
        if ((_dragFlags & DRAG_POS_X) == 0 && floatX > padLeft)
        {
            _floatLoc.x = padLeft;
        }
        else if ((_dragFlags & DRAG_NEG_X) == 0 && floatX < padLeft)
        {
            _floatLoc.x = padLeft;
        }

        // keep floating view from going past bottom of last header view
        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        final int firstPos = getFirstVisiblePosition();
        final int lastPos = getLastVisiblePosition();

        // Log.d("mobeta",
        // "nHead="+numHeaders+" nFoot="+numFooters+" first="+firstPos+" last="+lastPos);
        int topLimit = getPaddingTop();
        if (firstPos < numHeaders)
        {
            topLimit = getChildAt(numHeaders - firstPos - 1).getBottom();
        }
        if ((_dragFlags & DRAG_NEG_Y) == 0)
        {
            if (firstPos <= _srcPos)
            {
                topLimit = Math.max(getChildAt(_srcPos - firstPos).getTop(), topLimit);
            }
        }
        // bottom limit is top of first footer View or
        // bottom of last item in list
        int bottomLimit = getHeight() - getPaddingBottom();
        if (lastPos >= getCount() - numFooters - 1)
        {
            bottomLimit = getChildAt(getCount() - numFooters - 1 - firstPos).getBottom();
        }
        if ((_dragFlags & DRAG_POS_Y) == 0)
        {
            if (lastPos >= _srcPos)
            {
                bottomLimit = Math.min(getChildAt(_srcPos - firstPos).getBottom(), bottomLimit);
            }
        }

        if (floatY < topLimit)
        {
            _floatLoc.y = topLimit;
        }
        else if (floatY + _floatViewHeight > bottomLimit)
        {
            _floatLoc.y = bottomLimit - _floatViewHeight;
        }

        // get y-midpoint of floating view (constrained to ListView bounds)
        _floatViewMid = _floatLoc.y + _floatViewHeightHalf;
    }

    private void destroyFloatView()
    {
        if (_floatView != null)
        {
            _floatView.setVisibility(GONE);
            if (_floatViewManager != null)
            {
                _floatViewManager.onDestroyFloatView(_floatView);
            }
            _floatView = null;
            invalidate();
        }
    }

    /**
     * Interface for customization of the floating View appearance
     * and dragging behavior. Implement
     * your own and pass it to {@link #setFloatViewManager}. If
     * your own is not passed, the default {@link SimpleFloatViewManager}
     * implementation is used.
     */
    public interface FloatViewManager
    {
        /**
         * Return the floating View for item at <code>position</code>.
         * DragSortListView will measure and layout this View for you,
         * so feel free to just inflate it. You can help DSLV by
         * setting some {@link ViewGroup.LayoutParams} on this View;
         * otherwise it will set some for you (with a width of FILL_PARENT
         * and a height of WRAP_CONTENT).
         *
         * @param position Position of item to drag (NOTE:
         *                 <code>position</code> excludes header Views; thus, if you
         *                 want to call {@link ListView#getChildAt(int)}, you will need
         *                 to add {@link ListView#getHeaderViewsCount()} to the index).
         * @return The View you wish to display as the floating View.
         */
        public View onCreateFloatView(int position);

        /**
         * Called whenever the floating View is dragged. Float View
         * properties can be changed here. Also, the upcoming location
         * of the float View can be altered by setting
         * <code>location.x</code> and <code>location.y</code>.
         *
         * @param floatView     The floating View.
         * @param location      The location (top-left; relative to DSLV
         *                      top-left) at which the float
         *                      View would like to appear, given the current touch location
         *                      and the offset provided in {@link DragSortListView#startDrag}.
         * @param touch         The current touch location (relative to DSLV
         *                      top-left).
         */
        public void onDragFloatView(View floatView, Point location, Point touch);

        /**
         * Called when the float View is dropped; lets you perform
         * any necessary cleanup. The internal DSLV floating View
         * reference is set to null immediately after this is called.
         *
         * @param floatView The floating View passed to
         *                  {@link #onCreateFloatView(int)}.
         */
        public void onDestroyFloatView(View floatView);
    }

    public void setFloatViewManager(FloatViewManager manager)
    {
        _floatViewManager = manager;
    }

    public void setDragListener(DragListener l)
    {
        _dragListener = l;
    }

    /**
     * Allows for easy toggling between a DragSortListView
     * and a regular old ListView. If enabled, items are
     * draggable, where the drag init mode determines how
     * items are lifted (see setDragInitMode(int)).
     * If disabled, items cannot be dragged.
     *
     * @param enabled Set <code>true</code> to enable list
     *                item dragging
     */
    public void setDragEnabled(boolean enabled)
    {
        _dragEnabled = enabled;
    }

    public boolean isDragEnabled()
    {
        return _dragEnabled;
    }

    /**
     * This better reorder your ListAdapter! DragSortListView does not do this
     * for you; doesn't make sense to. Make sure
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it is called
     * in your implementation. Furthermore, if you have a choiceMode other than
     * none and the ListAdapter does not return true for
     * {@link ListAdapter#hasStableIds()}, you will need to call
     * {@link #moveCheckState(int, int)} to move the check boxes along with the
     * list items.
     *
     * @param l
     */
    public void setDropListener(DropListener l)
    {
        _dropListener = l;
    }

    /**
     * Probably a no-brainer, but make sure that your remove listener
     * calls {@link BaseAdapter#notifyDataSetChanged()} or something like it.
     * When an item removal occurs, DragSortListView
     * relies on a redraw of all the items to recover invisible views
     * and such. Strictly speaking, if you remove something, your dataset
     * has changed...
     *
     * @param l
     */
    public void setRemoveListener(RemoveListener l)
    {
        _removeListener = l;
    }

    public interface DragListener
    {
        public void drag(int from, int to);
    }

    /**
     * Your implementation of this has to reorder your ListAdapter!
     * Make sure to call
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it
     * in your implementation.
     *
     * @author heycosmo
     */
    public interface DropListener
    {
        public void drop(int from, int to);
    }

    /**
     * Make sure to call
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it
     * in your implementation.
     *
     * @author heycosmo
     */
    public interface RemoveListener
    {
        public void remove(int which);
    }

    public interface DragSortListener extends DropListener, DragListener, RemoveListener
    {
    }

    public void setDragSortListener(DragSortListener l)
    {
        setDropListener(l);
        setDragListener(l);
        setRemoveListener(l);
    }

    /**
     * Completely custom scroll speed profile. Default increases linearly
     * with position and is constant in time. Create your own by implementing
     * {@link DragSortListView.DragScrollProfile}.
     *
     * @param ssp
     */
    public void setDragScrollProfile(DragScrollProfile ssp)
    {
        if (ssp != null)
        {
            _scrollProfile = ssp;
        }
    }

    /**
     * Use this to move the check state of an item from one position to another
     * in a drop operation. If you have a choiceMode which is not none, this
     * method must be called when the order of items changes in an underlying
     * adapter which does not have stable IDs (see
     * {@link ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * <p/>
     * A word of warning about a "feature" in Android that you may run into when
     * dealing with movable list items: for an adapter that <em>does</em> have
     * stable IDs, ListView will attempt to locate each item based on its ID and
     * move the check state from the item's old position to the new position —
     * which is all fine and good (and removes the need for calling this
     * function), except for the half-baked approach. Apparently to save time in
     * the naive algorithm used, ListView will only search for an ID in the
     * close neighborhood of the old position. If the user moves an item too far
     * (specifically, more than 20 rows away), ListView will give up and just
     * force the item to be unchecked. So if there is a reasonable chance that
     * the user will move items more than 20 rows away from the original
     * position, you may wish to use an adapter with unstable IDs and call this
     * method manually instead.
     *
     * @param from
     * @param to
     */
    public void moveCheckState(int from, int to)
    {
        // This method runs in O(n log n) time (n being the number of list
        // items). The bottleneck is the call to AbsListView.setItemChecked,
        // which is O(log n) because of the binary search involved in calling
        // SparseBooleanArray.put().
        //
        // To improve on the average time, we minimize the number of calls to
        // setItemChecked by only calling it for items that actually have a
        // changed state. This is achieved by building a list containing the
        // start and end of the "runs" of checked items, and then moving the
        // runs. Note that moving an item from A to B is essentially a rotation
        // of the range of items in [A, B]. Let's say we have
        // . . U V X Y Z . .
        // and move U after Z. This is equivalent to a rotation one step to the
        // left within the range you are moving across:
        // . . V X Y Z U . .
        //
        // So, to perform the move we enumerate all the runs within the move
        // range, then rotate each run one step to the left or right (depending
        // on move direction). For example, in the list:
        // X X . X X X . X
        // we have two runs. One begins at the last item of the list and wraps
        // around to the beginning, ending at position 1. The second begins at
        // position 3 and ends at position 5. To rotate a run, regardless of
        // length, we only need to set a check mark at one end of the run, and
        // clear a check mark at the other end:
        // X . X X X . X X
        SparseBooleanArray cip = getCheckedItemPositions();
        int rangeStart = from;
        int rangeEnd = to;
        if (to < from)
        {
            rangeStart = to;
            rangeEnd = from;
        }
        rangeEnd += 1;

        int[] runStart = new int[cip.size()];
        int[] runEnd = new int[cip.size()];
        int runCount = buildRunList(cip, rangeStart, rangeEnd, runStart, runEnd);
        if (runCount == 1 && (runStart[0] == runEnd[0]))
        {
            // Special case where all items are checked, we can never set any
            // item to false like we do below.
            return;
        }

        if (from < to)
        {
            for (int i = 0; i != runCount; i++)
            {
                setItemChecked(rotate(runStart[i], -1, rangeStart, rangeEnd), true);
                setItemChecked(rotate(runEnd[i], -1, rangeStart, rangeEnd), false);
            }

        }
        else
        {
            for (int i = 0; i != runCount; i++)
            {
                setItemChecked(runStart[i], false);
                setItemChecked(runEnd[i], true);
            }
        }
    }

    /**
     * Use this when an item has been deleted, to move the check state of all
     * following items up one step. If you have a choiceMode which is not none,
     * this method must be called when the order of items changes in an
     * underlying adapter which does not have stable IDs (see
     * {@link ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * <p/>
     * See also further comments on {@link #moveCheckState(int, int)}.
     *
     * @param position
     */
    public void removeCheckState(int position)
    {
        SparseBooleanArray cip = getCheckedItemPositions();

        if (cip.size() == 0)
            return;
        int[] runStart = new int[cip.size()];
        int[] runEnd = new int[cip.size()];
        int rangeStart = position;
        int rangeEnd = cip.keyAt(cip.size() - 1) + 1;
        int runCount = buildRunList(cip, rangeStart, rangeEnd, runStart, runEnd);
        for (int i = 0; i != runCount; i++)
        {
            if (!(runStart[i] == position || (runEnd[i] < runStart[i] && runEnd[i] > position)))
            {
                // Only set a new check mark in front of this run if it does
                // not contain the deleted position. If it does, we only need
                // to make it one check mark shorter at the end.
                setItemChecked(rotate(runStart[i], -1, rangeStart, rangeEnd), true);
            }
            setItemChecked(rotate(runEnd[i], -1, rangeStart, rangeEnd), false);
        }
    }

    private static int buildRunList(SparseBooleanArray cip, int rangeStart,
                                    int rangeEnd, int[] runStart, int[] runEnd)
    {
        int runCount = 0;

        int i = findFirstSetIndex(cip, rangeStart, rangeEnd);
        if (i == -1)
            return 0;

        int position = cip.keyAt(i);
        int currentRunStart = position;
        int currentRunEnd = currentRunStart + 1;
        for (i++; i < cip.size() && (position = cip.keyAt(i)) < rangeEnd; i++)
        {
            if (!cip.valueAt(i)) // not checked => not interesting
                continue;
            if (position == currentRunEnd)
            {
                currentRunEnd++;
            }
            else
            {
                runStart[runCount] = currentRunStart;
                runEnd[runCount] = currentRunEnd;
                runCount++;
                currentRunStart = position;
                currentRunEnd = position + 1;
            }
        }

        if (currentRunEnd == rangeEnd)
        {
            // rangeStart and rangeEnd are equivalent positions so to be
            // consistent we translate them to the same integer value. That way
            // we can check whether a run covers the entire range by just
            // checking if the start equals the end position.
            currentRunEnd = rangeStart;
        }
        runStart[runCount] = currentRunStart;
        runEnd[runCount] = currentRunEnd;
        runCount++;

        if (runCount > 1)
        {
            if (runStart[0] == rangeStart && runEnd[runCount - 1] == rangeStart)
            {
                // The last run ends at the end of the range, and the first run
                // starts at the beginning of the range. So they are actually
                // part of the same run, except they wrap around the end of the
                // range. To avoid adjacent runs, we need to merge them.
                runStart[0] = runStart[runCount - 1];
                runCount--;
            }
        }
        return runCount;
    }

    private static int rotate(int value, int offset, int lowerBound, int upperBound)
    {
        int windowSize = upperBound - lowerBound;

        value += offset;
        if (value < lowerBound)
        {
            value += windowSize;
        }
        else if (value >= upperBound)
        {
            value -= windowSize;
        }
        return value;
    }

    private static int findFirstSetIndex(SparseBooleanArray sba, int rangeStart, int rangeEnd)
    {
        int size = sba.size();
        int i = insertionIndexForKey(sba, rangeStart);
        while (i < size && sba.keyAt(i) < rangeEnd && !sba.valueAt(i))
            i++;
        if (i == size || sba.keyAt(i) >= rangeEnd)
            return -1;
        return i;
    }

    private static int insertionIndexForKey(SparseBooleanArray sba, int key)
    {
        int low = 0;
        int high = sba.size();
        while (high - low > 0)
        {
            int middle = (low + high) >> 1;
            if (sba.keyAt(middle) < key)
                low = middle + 1;
            else
                high = middle;
        }
        return low;
    }

    /**
     * Interface for controlling
     * scroll speed as a function of touch position and time. Use
     * {@link DragSortListView#setDragScrollProfile(DragScrollProfile)} to
     * set custom profile.
     *
     * @author heycosmo
     */
    public interface DragScrollProfile
    {
        /**
         * Return a scroll speed in pixels/millisecond. Always return a
         * positive number.
         *
         * @param w Normalized position in scroll region (i.e. w \in [0,1]).
         *          Small w typically means slow scrolling.
         * @param t Time (in milliseconds) since start of scroll (handy if you
         *          want scroll acceleration).
         * @return Scroll speed at position w and time t in pixels/ms.
         */
        float getSpeed(float w, long t);
    }

    private class DragScroller implements Runnable
    {
        private boolean _abort;

        private long _prevTime;
        private long _currTime;

        private int _dy;
        private float _dt;
        private long _start;
        private int _scrollDir;

        public final static int STOP = -1;
        public final static int UP = 0;
        public final static int DOWN = 1;

        private float _scrollSpeed; // pixels per ms

        private boolean _scrolling = false;

        private int _lastHeader;
        private int _firstFooter;

        public boolean isScrolling()
        {
            return _scrolling;
        }

        public int getScrollDir()
        {
            return _scrolling ? _scrollDir : STOP;
        }

        public DragScroller()
        {
        }

        public void startScrolling(int dir)
        {
            if (!_scrolling)
            {
                // Debug.startMethodTracing("dslv-scroll");
                _abort = false;
                _scrolling = true;
                _start = SystemClock.uptimeMillis();
                _prevTime = _start;
                _scrollDir = dir;
                post(this);
            }
        }

        public void stopScrolling(boolean now)
        {
            if (now)
            {
                DragSortListView.this.removeCallbacks(this);
                _scrolling = false;
            }
            else
            {
                _abort = true;
            }
        }

        @Override
        public void run()
        {
            if (_abort)
            {
                _scrolling = false;
                return;
            }

            final int first = getFirstVisiblePosition();
            final int last = getLastVisiblePosition();
            final int count = getCount();
            final int padTop = getPaddingTop();
            final int listHeight = getHeight() - padTop - getPaddingBottom();

            int minY = Math.min(_y, _floatViewMid + _floatViewHeightHalf);
            int maxY = Math.max(_y, _floatViewMid - _floatViewHeightHalf);

            if (_scrollDir == UP)
            {
                View v = getChildAt(0);

                if (v == null)
                {
                    _scrolling = false;
                    return;
                }
                else
                {
                    if (first == 0 && v.getTop() == padTop)
                    {
                        _scrolling = false;
                        return;
                    }
                }
                _scrollSpeed = _scrollProfile.getSpeed((_upScrollStartYF - maxY)
                        / _dragUpScrollHeight, _prevTime);
            }
            else
            {
                View v = getChildAt(last - first);
                if (v == null)
                {
                    _scrolling = false;
                    return;
                }
                else
                {
                    if (last == count - 1 && v.getBottom() <= listHeight + padTop)
                    {
                        _scrolling = false;
                        return;
                    }
                }
                _scrollSpeed = -_scrollProfile.getSpeed((minY - _downScrollStartYF)
                        / _dragDownScrollHeight, _prevTime);
            }

            _currTime = SystemClock.uptimeMillis();
            _dt = (float) (_currTime - _prevTime);

            // dy is change in View position of a list item; i.e. positive dy
            // means user is scrolling up (list item moves down the screen,
            // remember
            // y=0 is at top of View).
            _dy = Math.round(_scrollSpeed * _dt);

            int movePos;
            if (_dy >= 0)
            {
                _dy = Math.min(listHeight, _dy);
                movePos = first;
            }
            else
            {
                _dy = Math.max(-listHeight, _dy);
                movePos = last;
            }

            final View moveItem = getChildAt(movePos - first);
            int top = moveItem.getTop() + _dy;

            if (movePos == 0 && top > padTop)
            {
                top = padTop;
            }

            // always do scroll
            _blockLayoutRequests = true;

            setSelectionFromTop(movePos, top - padTop);
            DragSortListView.this.layoutChildren();
            invalidate();

            _blockLayoutRequests = false;

            // scroll means relative float View movement
            doDragFloatView(movePos, moveItem, false);

            _prevTime = _currTime;

            post(this);
        }
    }

    private class DragSortTracker
    {
        StringBuilder _stringBuilder = new StringBuilder();

        File _file;

        private int _numInBuffer = 0;
        private int _numFlushes = 0;

        private boolean _tracking = false;

        public DragSortTracker()
        {
            File root = Environment.getExternalStorageDirectory();
            _file = new File(root, "dslv_state.txt");

            if (!_file.exists())
            {
                try
                {
                    _file.createNewFile();
                    Log.d("mobeta", "file created");
                }
                catch (IOException e)
                {
                    Log.w("mobeta", "Could not create dslv_state.txt");
                    Log.d("mobeta", e.getMessage());
                }
            }
        }

        public void startTracking()
        {
            _stringBuilder.append("<DSLVStates>\n");
            _numFlushes = 0;
            _tracking = true;
        }

        public void appendState()
        {
            if (!_tracking)
            {
                return;
            }

            _stringBuilder.append("<DSLVState>\n");
            final int children = getChildCount();
            final int first = getFirstVisiblePosition();
            _stringBuilder.append("    <Positions>");
            for (int i = 0; i < children; ++i)
            {
                _stringBuilder.append(first + i).append(",");
            }
            _stringBuilder.append("</Positions>\n");

            _stringBuilder.append("    <Tops>");
            for (int i = 0; i < children; ++i)
            {
                _stringBuilder.append(getChildAt(i).getTop()).append(",");
            }
            _stringBuilder.append("</Tops>\n");
            _stringBuilder.append("    <Bottoms>");
            for (int i = 0; i < children; ++i)
            {
                _stringBuilder.append(getChildAt(i).getBottom()).append(",");
            }
            _stringBuilder.append("</Bottoms>\n");

            _stringBuilder.append("    <FirstExpPos>").append(_firstExpPos).append("</FirstExpPos>\n");
            _stringBuilder.append("    <FirstExpBlankHeight>")
                    .append(getItemHeight(_firstExpPos) - getChildHeight(_firstExpPos))
                    .append("</FirstExpBlankHeight>\n");
            _stringBuilder.append("    <SecondExpPos>").append(_secondExpPos).append("</SecondExpPos>\n");
            _stringBuilder.append("    <SecondExpBlankHeight>")
                    .append(getItemHeight(_secondExpPos) - getChildHeight(_secondExpPos))
                    .append("</SecondExpBlankHeight>\n");
            _stringBuilder.append("    <SrcPos>").append(_srcPos).append("</SrcPos>\n");
            _stringBuilder.append("    <SrcHeight>").append(_floatViewHeight + getDividerHeight())
                    .append("</SrcHeight>\n");
            _stringBuilder.append("    <ViewHeight>").append(getHeight()).append("</ViewHeight>\n");
            _stringBuilder.append("    <LastY>").append(_lastY).append("</LastY>\n");
            _stringBuilder.append("    <FloatY>").append(_floatViewMid).append("</FloatY>\n");
            _stringBuilder.append("    <ShuffleEdges>");
            for (int i = 0; i < children; ++i)
            {
                _stringBuilder.append(getShuffleEdge(first + i, getChildAt(i).getTop())).append(",");
            }
            _stringBuilder.append("</ShuffleEdges>\n");

            _stringBuilder.append("</DSLVState>\n");
            _numInBuffer++;

            if (_numInBuffer > 1000)
            {
                flush();
                _numInBuffer = 0;
            }
        }

        public void flush()
        {
            if (!_tracking)
            {
                return;
            }

            // save to file on sdcard
            try
            {
                boolean append = true;
                if (_numFlushes == 0)
                {
                    append = false;
                }
                FileWriter writer = new FileWriter(_file, append);

                writer.write(_stringBuilder.toString());
                _stringBuilder.delete(0, _stringBuilder.length());

                writer.flush();
                writer.close();

                _numFlushes++;
            }
            catch (IOException e)
            {
                // do nothing
            }
        }

        public void stopTracking()
        {
            if (_tracking)
            {
                _stringBuilder.append("</DSLVStates>\n");
                flush();
                _tracking = false;
            }
        }
    }
}
