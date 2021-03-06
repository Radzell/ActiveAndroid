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

import android.content.Context;
import android.graphics.Bitmap;

import com.activeandroid.serializer.CalendarSerializer;
import com.activeandroid.serializer.FileSerializer;
import com.activeandroid.serializer.SqlDateSerializer;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.serializer.UtilDateSerializer;
import com.activeandroid.sqlserializer.BitmapSerializerSql;
import com.activeandroid.sqlserializer.BooleanSerializerSql;
import com.activeandroid.sqlserializer.DateSerializerSql;
import com.activeandroid.sqlserializer.DoubleSerializerSql;
import com.activeandroid.sqlserializer.FloatSerializerSql;
import com.activeandroid.sqlserializer.IntSerializerSql;
import com.activeandroid.sqlserializer.LongSerializerSql;
import com.activeandroid.sqlserializer.SqlTypeSerializer;
import com.activeandroid.sqlserializer.StringSerializerSql;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexFile;

final class ModelInfo {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////
    private Map<String, TableInfo> mTableInfos = new HashMap<String, TableInfo>();

	private Map<Class<? extends Model>, String> mModelTableInfos = new HashMap<Class<? extends Model>, String>();
	private Map<Class<?>, TypeSerializer> mTypeSerializers = new HashMap<Class<?>, TypeSerializer>() {
		{
			put(Calendar.class, new CalendarSerializer());
			put(java.sql.Date.class, new SqlDateSerializer());
			put(java.util.Date.class, new UtilDateSerializer());
			put(java.io.File.class, new FileSerializer());
		}
	};

    private Map<Class, SqlTypeSerializer> mSqlTypeSerializers = new ConcurrentHashMap<Class, SqlTypeSerializer>(){
        {
            put(int.class, new IntSerializerSql());
            put(Integer.class, new IntSerializerSql());

            put(long.class, new LongSerializerSql());
            put(Long.class, new LongSerializerSql());

            put(float.class, new FloatSerializerSql());
            put(Float.class, new FloatSerializerSql());

            put(double.class, new DoubleSerializerSql());
            put(Double.class, new DoubleSerializerSql());

            put(boolean.class, new BooleanSerializerSql());
            put(Boolean.class, new BooleanSerializerSql());

            put(String.class, new StringSerializerSql());
            put(Date.class, new DateSerializerSql());
            put(Bitmap.class, new BitmapSerializerSql());
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ModelInfo(Configuration configuration) {
		if (!loadModelFromMetaData(configuration)) {
            loadFromContext(configuration.getContext());
		}

		Log.i("ModelInfo loaded.");

    }



    public ModelInfo(Context context){
        loadFromContext(context);
        Log.i("ModelInfo loaded.");
    }

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public Collection<TableInfo> getTableInfos() {
		return mTableInfos.values();
	}
    public java.util.Set<Class<? extends Model>> getModels() {
        return mModelTableInfos.keySet();
    }

	public TableInfo getTableInfo(Class<? extends Model> type) {
		return mTableInfos.get(mModelTableInfos.get(type));
	}

	public TypeSerializer getTypeSerializer(Class<?> type) {
		return mTypeSerializers.get(type);
	}

    public SqlTypeSerializer getSQLTypeSerializer(Class<?> type) {
        return mSqlTypeSerializers.get(type);
    }
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////
    private void loadFromContext(Context context) {
        try {
            scanForModel(context);
        } catch (IOException e) {
            Log.e("Couldn't open source path.", e);
        }
    }

	private boolean loadModelFromMetaData(Configuration configuration) {
		if (!configuration.isValid()) {
			return false;
		}

		final List<Class<? extends Model>> models = configuration.getModelClasses();
		if (models != null) {
			for (Class<? extends Model> model : models) {
                TableInfo tableinfo = new TableInfo(model);
                if(mTableInfos.containsKey(tableinfo.getTableName())){
                    TableInfo oldtableInfo = mTableInfos.get(tableinfo.getTableName());
                    oldtableInfo.addColumns(tableinfo.getColumns());
                    oldtableInfo.addType(tableinfo.getPimraryType());
                    tableinfo = oldtableInfo;
                }else{
                    mTableInfos.put(tableinfo.getTableName(), tableinfo);
                }
                mModelTableInfos.put(model,tableinfo.getTableName());
			}
		}

		final List<Class<? extends TypeSerializer>> typeSerializers = configuration.getTypeSerializers();
		if (typeSerializers != null) {
			for (Class<? extends TypeSerializer> typeSerializer : typeSerializers) {
				try {
					TypeSerializer instance = typeSerializer.newInstance();
					mTypeSerializers.put(instance.getDeserializedType(), instance);
				}
				catch (InstantiationException e) {
					Log.e("Couldn't instantiate TypeSerializer.", e);
				}
				catch (IllegalAccessException e) {
					Log.e("IllegalAccessException", e);
				}
			}
		}

		return true;
	}

	private void scanForModel(Context context) throws IOException {
		String packageName = context.getPackageName();
		String sourcePath = context.getApplicationInfo().sourceDir;
		List<String> paths = new ArrayList<String>();

		if (sourcePath != null && !(new File(sourcePath).isDirectory())) {
			DexFile dexfile = new DexFile(sourcePath);
			Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				paths.add(entries.nextElement());
			}
		}
		// Robolectric fallback
		else {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> resources = classLoader.getResources("");

			while (resources.hasMoreElements()) {
				String path = resources.nextElement().getFile();
				if (path.contains("bin") || path.contains("classes")) {
					paths.add(path);
				}
			}
		}

		for (String path : paths) {
			File file = new File(path);
			scanForModelClasses(file, packageName, context.getClassLoader());
		}
	}

	private void scanForModelClasses(File path, String packageName, ClassLoader classLoader) {
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				scanForModelClasses(file, packageName, classLoader);
			}
		}
		else {
			String className = path.getName();

			// Robolectric fallback
			if (!path.getPath().equals(className)) {
				className = path.getPath();

				if (className.endsWith(".class")) {
					className = className.substring(0, className.length() - 6);
				}
				else {
					return;
				}

				className = className.replace(System.getProperty("file.separator"), ".");

				int packageNameIndex = className.lastIndexOf(packageName);
				if (packageNameIndex < 0) {
					return;
				}

				className = className.substring(packageNameIndex);
			}

			try {
				Class<?> discoveredClass = Class.forName(className, false, classLoader);
				if (ReflectionUtils.isModel(discoveredClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
                    TableInfo tableinfo = new TableInfo(modelClass);
                    if(mTableInfos.containsKey(tableinfo.getTableName())){
                        TableInfo oldtableInfo = mTableInfos.get(tableinfo.getTableName());
                        oldtableInfo.addColumns(tableinfo.getColumns());
                        oldtableInfo.addType(tableinfo.getPimraryType());
                        tableinfo = oldtableInfo;
                    }else{
                        mTableInfos.put(tableinfo.getTableName(), tableinfo);
                    }
                    mModelTableInfos.put(modelClass,tableinfo.getTableName());
				}
				else if (ReflectionUtils.isTypeSerializer(discoveredClass)) {
					TypeSerializer instance = (TypeSerializer) discoveredClass.newInstance();
					mTypeSerializers.put(instance.getDeserializedType(), instance);
				}
			}
			catch (ClassNotFoundException e) {
				Log.e("Couldn't create class.", e);
			}
			catch (InstantiationException e) {
				Log.e("Couldn't instantiate TypeSerializer.", e);
			}
			catch (IllegalAccessException e) {
				Log.e("IllegalAccessException", e);
			}catch (NoClassDefFoundError e){
                Log.e("NoClassDefFoundError", e);
            }
		}
	}


}
