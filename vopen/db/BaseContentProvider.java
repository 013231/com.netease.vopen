
package vopen.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import common.pal.PalLog;

/**
 * ContentProvider基础类.
 * 
 * @author wjying
 */
public abstract class BaseContentProvider extends ContentProvider {
    private static final String TAG = "BaseContentProvider";

    public static final String PARAMETER_NOTIFY = "notify";

    private SQLiteOpenHelper mOpenHelper;


    /**
     * 子类应该通过本方法设置SQLiteOpenHelper
     * 
     * @param helper
     */
    public void setSQLiteOpenHelper(SQLiteOpenHelper helper) {
        mOpenHelper = helper;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = null;
        Cursor result = null;
        try {
            db = mOpenHelper.getWritableDatabase();// .getReadableDatabase();
            result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
            if (result != null) {
                result.setNotificationUri(getContext().getContentResolver(), uri);
            }
        } catch (SQLiteDiskIOException e) {
           PalLog.i(TAG, "query : " + e.toString());
            if (result != null) {
                result.close();
                result = null;
            }
        }

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);
        if (rowId <= 0)
            return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0)
                    return 0;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        try {
            count = db.delete(args.table, args.where, args.args);
        } catch (SQLiteDiskIOException e) {
        	PalLog.i(TAG, "delete : " + e.toString());
        }
        // if (count > 0) sendNotify(uri);
        sendNotify(uri); // TODO 为什么对整个表操作返回count=0
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;
        try {
            count = db.update(args.table, values, args.where, args.args);
        } catch (SQLiteDiskIOException e) {
        	PalLog.i(TAG, "update : " + e.toString());
        }
        // if (count > 0) sendNotify(uri);
        sendNotify(uri); // TODO 为什么对整个表操作返回count=0
        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }    

    public static class SqlArguments {
        public final String table;

        public final String where;

        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
