package com.activeandroid;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class TableInfo {
    //////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Class<? extends Model> mType;
	private String mTableName;
	private String mIdName = Table.DEFAULT_ID_NAME;
	private Set<ColumnField> mColumns = new LinkedHashSet<ColumnField>();
    private ColumnField mMatchValue;
    private ColumnField idColumnField;

    //////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public TableInfo(Class<? extends Model> type) {
		mType = type;

		final Table tableAnnotation = type.getAnnotation(Table.class);

        if (tableAnnotation != null) {
			mTableName = tableAnnotation.name();
			mIdName = tableAnnotation.id();
		}
		else {
			mTableName = type.getSimpleName();
        }

        // Manually add the id column since it is not declared like the other columns.
        Field idField = getIdField(type);
        idColumnField = new ColumnField(mIdName, idField,false,true);
        mColumns.add(idColumnField);

        List<Field> fields = new LinkedList<Field>(ReflectionUtils.getDeclaredColumnFields(type));
        Collections.reverse(fields);

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                final Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                if (TextUtils.isEmpty(columnName)) {
                    columnName = field.getName();
                }
                if(columnAnnotation.matchvalue()&&mMatchValue==null){
                    mMatchValue= new ColumnField(columnName,field,true);
                }
                mColumns.add(new ColumnField(columnName, field));
            }
        }
        if(mMatchValue==null){
            mMatchValue=idColumnField;
        }
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public Class<? extends Model> getType() {
		return mType;
	}

	public String getTableName() {
		return mTableName;
	}

	public String getIdName() {
		return mIdName;
	}



    private Field getIdField(Class<?> type) {
        if (type.equals(Model.class)) {
            try {
                return type.getDeclaredField("mId");
            }
            catch (NoSuchFieldException e) {
                Log.e("Impossible!", e.toString());
            }
        }
        else if (type.getSuperclass() != null) {
            return getIdField(type.getSuperclass());
        }

        return null;
    }

    public boolean hasMatchValue() {
        return (mMatchValue!=null);
    }

    public ColumnField getMatchValue() {
        return mMatchValue;
    }

    public Set<ColumnField> getColumns() {
        return mColumns;
    }

    public static class ColumnField {
        final String name;
        String sqlType;
        final Field field;
        final boolean isMatchValue;
        final boolean isAutoIncrement;


        public ColumnField(String name, Field field){
            this(name, field, false, false);
        }
        public ColumnField(String name, Field field,boolean isMatchValue){
            this(name, field, isMatchValue, false);
        }
        public ColumnField(String name, Field field, boolean isMatchValue, boolean isAutoIncrement) {
            this.name = name;
            this.field = field;
            this.isMatchValue = isMatchValue;
            this.isAutoIncrement = isAutoIncrement;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ColumnField) {
                return ((ColumnField) o).name.equals(name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        public Field getField() {
            return field;
        }

        public String getName() {
            return name;
        }
    }

}
