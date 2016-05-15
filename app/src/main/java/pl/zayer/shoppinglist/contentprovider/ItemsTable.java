package pl.zayer.shoppinglist.contentprovider;

import android.database.sqlite.SQLiteDatabase;

/**
 * Class describing shopping lists items table in SQLite database.
 * Created by ZaYeR on 2016-05-12.
 */
public class ItemsTable {

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SHOPPING_LIST_ID = "shopping_list_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CHECKED = "checked";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String[] COLUMNS_ALL = {
            COLUMN_ID,
            COLUMN_SHOPPING_LIST_ID,
            COLUMN_CONTENT,
            COLUMN_CHECKED,
            COLUMN_TIMESTAMP,
            TABLE_ITEMS + "." + COLUMN_ID
    };

    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_SHOPPING_LIST_ID + " INTEGER,"
            + COLUMN_CONTENT + " TEXT,"
            + COLUMN_CHECKED + " INTEGER DEFAULT 0,"
            + COLUMN_TIMESTAMP + " DATETIME"
            + ")";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_ITEMS);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(sqLiteDatabase);
    }
}
