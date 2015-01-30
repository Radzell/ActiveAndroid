package com.activeandroid.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.activeandroid.Cache;
import com.activeandroid.Model;

import java.util.List;

import static com.activeandroid.util.SQLiteUtils.processCursor;

/**
 * Created by radzell on 12/4/14.
 */
public class ModelCursorAdapter<T extends Model>  extends CursorAdapter {
    private T t;

    public ModelCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public T getItem(int position) {
        Cursor cursor = (Cursor) super.getItem(position);
        List<T> entities = processCursor(t.getClass(), cursor);
        if(!entities.isEmpty()) {
            return entities.get(0);
        }else{
            return null;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }


}
