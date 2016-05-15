package pl.zayer.shoppinglist.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for SQLite database.
 * Created by ZaYeR on 2016-05-12.
 */
public class ShoppingListsDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shoppingList";

    public ShoppingListsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        ShoppingListsTable.onCreate(sqLiteDatabase);
        ItemsTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        ShoppingListsTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        ItemsTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }
}
