package ch.icarosdev.pagesdatabase;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 06.10.13
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "pagesManager";
    // Contacts table name
    private static final String TABLE_PAGES = "pages";
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_CATEGORY = "category";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_URL + " TEXT"
                + KEY_CATEGORY + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGES);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new page
    void addPage(Page page) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = this.getUpdateValues(page);

        // Inserting Row
        db.insert(TABLE_PAGES, null, values);
        db.close(); // Closing database connection
    }

    private ContentValues getUpdateValues(Page page) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, page.getName());
        values.put(KEY_URL, page.getUrl());
        values.put(KEY_CATEGORY, page.getCategory());

        return values;
    }

    Page getPage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PAGES, new String[]{KEY_ID,
                KEY_NAME, KEY_URL, KEY_CATEGORY}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Page page = this.buildPage(cursor);
        // return page
        return page;
    }

    private Page buildPage(Cursor cursor) {
        return new Page(
                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_URL)),
                cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
        );
    }

    // Getting All Contacts
    public List<Page> getAllPages() {
        List<Page> pageList = new ArrayList<Page>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Page page = this.buildPage(cursor);
                // Adding page to list
                pageList.add(page);
            } while (cursor.moveToNext());
        }

        // return page list
        return pageList;
    }

    // Updating single page
    public int updatePage(Page page) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = this.getUpdateValues(page);

        // updating row
        return db.update(TABLE_PAGES, values, KEY_ID + " = ?", new String[]{String.valueOf(page.getId())});
    }

    // Deleting single page
    public void deletePage(Page page) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PAGES, KEY_ID + " = ?", new String[]{String.valueOf(page.getId())});
        db.close();
    }

    // Getting pages Count
    public int getPagesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
