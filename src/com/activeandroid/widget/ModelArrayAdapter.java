package com.activeandroid.widget;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.activeandroid.Model;

public class ModelArrayAdapter<T extends Model> extends ArrayAdapter<T> {
	public ModelArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public ModelArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	public ModelArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
		super(context, textViewResourceId, objects);
	}

	public ModelArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	/**
	 * Clears the adapter and, if data != null, fills if with new Items.
	 *
	 * @param collection A Collection&lt;? extends T&gt; which members get added to the adapter.
	 */
	public void setData(Collection<? extends T> collection) {
		clear();

		if (collection != null) {
			for (T item : collection) {
				add(item);
			}
		}
	}

	/**
	 * @return The Id of the record at position.
	 */
	@Override
	public long getItemId(int position) {
		T item = getItem(position);

		if (item != null) {
			return item.getId();
		}
		else {
			return -1;
		}
	}
}
