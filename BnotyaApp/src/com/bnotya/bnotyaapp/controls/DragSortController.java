package com.bnotya.bnotyaapp.controls;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;

public class DragSortController extends SimpleFloatViewManager implements View.OnTouchListener, GestureDetector.OnGestureListener
{
    /**
     * Drag init mode enum.
     */
    public static final int ON_DOWN = 0;
    public static final int ON_DRAG = 1;
    public static final int ON_LONG_PRESS = 2;

    private int _dragInitMode = ON_DOWN;

    private boolean _sortEnabled = true;

    /**
     * Remove mode enum.
     */
    public static final int CLICK_REMOVE = 0;
    public static final int FLING_REMOVE = 1;

    /**
     * The current remove mode.
     */
    private int _removeMode;

    private boolean _removeEnabled = false;
    private boolean _isRemoving = false;

    private GestureDetector _detector;

    private GestureDetector _flingRemoveDetector;

    private int _touchSlop;

    public static final int MISS = -1;

    private int _hitPos = MISS;
    private int _flingHitPos = MISS;

    private int _clickRemoveHitPos = MISS;

    private int[] _tempLoc = new int[2];

    private int _itemX;
    private int _itemY;

    private int _currX;
    private int _currY;

    private boolean _dragging = false;

    float _flingSpeed = 500f;

    private int _dragHandleId;

    private int _clickRemoveId;

    private int _flingHandleId;
    private boolean _canDrag;

    private DragSortListView _dragSortListView;
    private int _positionX;

    /**
     * Calls DragSortController(DragSortListView, int) with a
     * 0 drag handle id, FLING_RIGHT_REMOVE remove mode,
     * and ON_DOWN drag init. By default, sorting is enabled, and
     * removal is disabled.
     *
     * @param dslv The DSLV instance
     */
    public DragSortController(DragSortListView dslv)
    {
        this(dslv, 0, ON_DOWN, FLING_REMOVE);
    }

    public DragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode, int removeMode)
    {
        this(dslv, dragHandleId, dragInitMode, removeMode, 0);
    }

    public DragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode, int removeMode, int clickRemoveId)
    {
        this(dslv, dragHandleId, dragInitMode, removeMode, clickRemoveId, 0);
    }

    /**
     * By default, sorting is enabled, and removal is disabled.
     *
     * @param dslv         The DSLV instance
     * @param dragHandleId The resource id of the View that represents
     *                     the drag handle in a list item.
     */
    public DragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode,
                              int removeMode, int clickRemoveId, int flingHandleId)
    {
        super(dslv);
        _dragSortListView = dslv;
        _detector = new GestureDetector(dslv.getContext(), this);
        _flingRemoveDetector = new GestureDetector(dslv.getContext(), _flingRemoveListener);
        _flingRemoveDetector.setIsLongpressEnabled(false);
        _touchSlop = ViewConfiguration.get(dslv.getContext()).getScaledTouchSlop();
        _dragHandleId = dragHandleId;
        _clickRemoveId = clickRemoveId;
        _flingHandleId = flingHandleId;
        setRemoveMode(removeMode);
        setDragInitMode(dragInitMode);
    }


    public int getDragInitMode()
    {
        return _dragInitMode;
    }

    /**
     * Set how a drag is initiated. Needs to be one of
     * ON_DOWN, ON_DRAG, or ON_LONG_PRESS.
     *
     * @param mode The drag init mode.
     */
    public void setDragInitMode(int mode)
    {
        _dragInitMode = mode;
    }

    /**
     * Enable/Disable list item sorting. Disabling is useful if only item
     * removal is desired. Prevents drags in the vertical direction.
     *
     * @param enabled Set <code>true</code> to enable list
     *                item sorting.
     */
    public void setSortEnabled(boolean enabled)
    {
        _sortEnabled = enabled;
    }

    public boolean isSortEnabled()
    {
        return _sortEnabled;
    }

    /**
     * One of CLICK_REMOVE, FLING_RIGHT_REMOVE,
     * FLING_LEFT_REMOVE,
     * SLIDE_RIGHT_REMOVE, or SLIDE_LEFT_REMOVE.
     */
    public void setRemoveMode(int mode)
    {
        _removeMode = mode;
    }

    public int getRemoveMode()
    {
        return _removeMode;
    }

    /**
     * Enable/Disable item removal without affecting remove mode.
     */
    public void setRemoveEnabled(boolean enabled)
    {
        _removeEnabled = enabled;
    }

    public boolean isRemoveEnabled()
    {
        return _removeEnabled;
    }

    /**
     * Set the resource id for the View that represents the drag
     * handle in a list item.
     *
     * @param id An android resource id.
     */
    public void setDragHandleId(int id)
    {
        _dragHandleId = id;
    }

    /**
     * Set the resource id for the View that represents the fling
     * handle in a list item.
     *
     * @param id An android resource id.
     */
    public void setFlingHandleId(int id)
    {
        _flingHandleId = id;
    }

    /**
     * Set the resource id for the View that represents click
     * removal button.
     *
     * @param id An android resource id.
     */
    public void setClickRemoveId(int id)
    {
        _clickRemoveId = id;
    }

    /**
     * Sets flags to restrict certain motions of the floating View
     * based on DragSortController settings (such as remove mode).
     * Starts the drag on the DragSortListView.
     *
     * @param position The list item position (includes headers).
     * @param deltaX   Touch x-coord minus left edge of floating View.
     * @param deltaY   Touch y-coord minus top edge of floating View.
     * @return True if drag started, false otherwise.
     */
    public boolean startDrag(int position, int deltaX, int deltaY)
    {

        int dragFlags = 0;
        if (_sortEnabled && !_isRemoving)
        {
            dragFlags |= DragSortListView.DRAG_POS_Y | DragSortListView.DRAG_NEG_Y;
        }
        if (_removeEnabled && _isRemoving)
        {
            dragFlags |= DragSortListView.DRAG_POS_X;
            dragFlags |= DragSortListView.DRAG_NEG_X;
        }

        _dragging = _dragSortListView.startDrag(position - _dragSortListView.getHeaderViewsCount(), dragFlags, deltaX,
                deltaY);
        return _dragging;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev)
    {
        if (!_dragSortListView.isDragEnabled() || _dragSortListView.listViewIntercepted())
        {
            return false;
        }

        _detector.onTouchEvent(ev);
        if (_removeEnabled && _dragging && _removeMode == FLING_REMOVE)
        {
            _flingRemoveDetector.onTouchEvent(ev);
        }

        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                _currX = (int) ev.getX();
                _currY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (_removeEnabled && _isRemoving)
                {
                    int x = _positionX >= 0 ? _positionX : -_positionX;
                    int removePoint = _dragSortListView.getWidth() / 2;
                    if (x > removePoint)
                    {
                        _dragSortListView.stopDragWithVelocity(true, 0);
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                _isRemoving = false;
                _dragging = false;
                break;
        }

        return false;
    }

    /**
     * Overrides to provide fading when slide removal is enabled.
     */
    @Override
    public void onDragFloatView(View floatView, Point position, Point touch)
    {

        if (_removeEnabled && _isRemoving)
        {
            _positionX = position.x;
        }
    }

    /**
     * Get the position to start dragging based on the ACTION_DOWN
     * MotionEvent. This function simply calls
     * {@link #dragHandleHitPosition(MotionEvent)}. Override
     * to change drag handle behavior;
     * this function is called internally when an ACTION_DOWN
     * event is detected.
     *
     * @param ev The ACTION_DOWN MotionEvent.
     * @return The list position to drag if a drag-init gesture is
     * detected; MISS if unsuccessful.
     */
    public int startDragPosition(MotionEvent ev)
    {
        return dragHandleHitPosition(ev);
    }

    public int startFlingPosition(MotionEvent ev)
    {
        return _removeMode == FLING_REMOVE ? flingHandleHitPosition(ev) : MISS;
    }

    /**
     * Checks for the touch of an item's drag handle (specified by
     * {@link #setDragHandleId(int)}), and returns that item's position
     * if a drag handle touch was detected.
     *
     * @param ev The ACTION_DOWN MotionEvent.
     * @return The list position of the item whose drag handle was
     * touched; MISS if unsuccessful.
     */
    public int dragHandleHitPosition(MotionEvent ev)
    {
        return viewIdHitPosition(ev, _dragHandleId);
    }

    public int flingHandleHitPosition(MotionEvent ev)
    {
        return viewIdHitPosition(ev, _flingHandleId);
    }

    public int viewIdHitPosition(MotionEvent ev, int id)
    {
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();

        int touchPos = _dragSortListView.pointToPosition(x, y); // includes headers/footers

        final int numHeaders = _dragSortListView.getHeaderViewsCount();
        final int numFooters = _dragSortListView.getFooterViewsCount();
        final int count = _dragSortListView.getCount();

        // We're only interested if the touch was on an
        // item that's not a header or footer.
        if (touchPos != AdapterView.INVALID_POSITION && touchPos >= numHeaders
                && touchPos < (count - numFooters))
        {
            final View item = _dragSortListView.getChildAt(touchPos - _dragSortListView.getFirstVisiblePosition());
            final int rawX = (int) ev.getRawX();
            final int rawY = (int) ev.getRawY();

            View dragBox = id == 0 ? item : item.findViewById(id);
            if (dragBox != null)
            {
                dragBox.getLocationOnScreen(_tempLoc);

                if (rawX > _tempLoc[0] && rawY > _tempLoc[1] &&
                        rawX < _tempLoc[0] + dragBox.getWidth() &&
                        rawY < _tempLoc[1] + dragBox.getHeight())
                {

                    _itemX = item.getLeft();
                    _itemY = item.getTop();

                    return touchPos;
                }
            }
        }

        return MISS;
    }

    @Override
    public boolean onDown(MotionEvent ev)
    {
        if (_removeEnabled && _removeMode == CLICK_REMOVE)
        {
            _clickRemoveHitPos = viewIdHitPosition(ev, _clickRemoveId);
        }

        _hitPos = startDragPosition(ev);
        if (_hitPos != MISS && _dragInitMode == ON_DOWN)
        {
            startDrag(_hitPos, (int) ev.getX() - _itemX, (int) ev.getY() - _itemY);
        }

        _isRemoving = false;
        _canDrag = true;
        _positionX = 0;
        _flingHitPos = startFlingPosition(ev);

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        final int x1 = (int) e1.getX();
        final int y1 = (int) e1.getY();
        final int x2 = (int) e2.getX();
        final int y2 = (int) e2.getY();
        final int deltaX = x2 - _itemX;
        final int deltaY = y2 - _itemY;

        if (_canDrag && !_dragging && (_hitPos != MISS || _flingHitPos != MISS))
        {
            if (_hitPos != MISS)
            {
                if (_dragInitMode == ON_DRAG && Math.abs(y2 - y1) > _touchSlop && _sortEnabled)
                {
                    startDrag(_hitPos, deltaX, deltaY);
                }
                else if (_dragInitMode != ON_DOWN && Math.abs(x2 - x1) > _touchSlop && _removeEnabled)
                {
                    _isRemoving = true;
                    startDrag(_flingHitPos, deltaX, deltaY);
                }
            }
            else if (_flingHitPos != MISS)
            {
                if (Math.abs(x2 - x1) > _touchSlop && _removeEnabled)
                {
                    _isRemoving = true;
                    startDrag(_flingHitPos, deltaX, deltaY);
                }
                else if (Math.abs(y2 - y1) > _touchSlop)
                {
                    _canDrag = false; // if started to scroll the list then
                    // don't allow sorting nor fling-removing
                }
            }
        }
        // return whatever
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        if (_hitPos != MISS && _dragInitMode == ON_LONG_PRESS)
        {
            _dragSortListView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            startDrag(_hitPos, _currX - _itemX, _currY - _itemY);
        }
    }

    // complete the OnGestureListener interface
    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return false;
    }

    // complete the OnGestureListener interface
    @Override
    public boolean onSingleTapUp(MotionEvent ev)
    {
        if (_removeEnabled && _removeMode == CLICK_REMOVE)
        {
            if (_clickRemoveHitPos != MISS)
            {
                _dragSortListView.removeItem(_clickRemoveHitPos - _dragSortListView.getHeaderViewsCount());
            }
        }
        return true;
    }

    // complete the OnGestureListener interface
    @Override
    public void onShowPress(MotionEvent ev)
    {
        // do nothing
    }

    GestureDetector.OnGestureListener _flingRemoveListener =
            new GestureDetector.SimpleOnGestureListener()
            {
                @Override
                public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                             float velocityY)
                {
                    if (_removeEnabled && _isRemoving)
                    {
                        int w = _dragSortListView.getWidth();
                        int minPos = w / 5;
                        if (velocityX > _flingSpeed)
                        {
                            if (_positionX > -minPos)
                            {
                                _dragSortListView.stopDragWithVelocity(true, velocityX);
                            }
                        }
                        else if (velocityX < -_flingSpeed)
                        {
                            if (_positionX < minPos)
                            {
                                _dragSortListView.stopDragWithVelocity(true, velocityX);
                            }
                        }
                        _isRemoving = false;
                    }
                    return false;
                }
            };
}
