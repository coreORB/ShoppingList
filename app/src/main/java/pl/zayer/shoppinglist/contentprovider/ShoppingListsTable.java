package pl.zayer.shoppinglist.contentprovider;

import android.database.sqlite.SQLiteDatabase;

/**
 * Class describing shopping lists table in SQLite database.
 * Created by ZaYeR on 2016-05-12.
 */
public class ShoppingListsTable {

    public static final String TABLE_SHOPPING_LISTS = "shopping_lists";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String[] COLUMNS_ALL = {
            COLUMN_ID,
            COLUMN_TITLE,
            COLUMN_ARCHIVED,
            COLUMN_CREATED_AT,
            TABLE_SHOPPING_LISTS + "." + COLUMN_ID
    };

    private static final String CREATE_TABLE_SHOPPING_LISTS = "CREATE TABLE " + TABLE_SHOPPING_LISTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_ARCHIVED + " INTEGER DEFAULT 0,"
            + COLUMN_CREATED_AT + " DATETIME"
            + ")";

    public static void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_SHOPPING_LISTS);
    }

    public static void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPPING_LISTS);
        onCreate(sqLiteDatabase);
    }

}
