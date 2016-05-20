package pl.coreorb.shoppinglist.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Content Provider class for shopping lists.
 * Created by ZaYeR on 2016-05-12.
 */
public class ShoppingListsContentProvider extends ContentProvider {

    private static final String LOG_TAG = ShoppingListsContentProvider.class.getSimpleName();
    private ShoppingListsDatabaseHelper database;

    private static final int SHOPPING_LISTS = 10;
    private static final int SHOPPING_LIST_ID = 20;
    private static final int ITEMS = 30;
    private static final int ITEM_ID = 40;
    private static final int SHOPPING_LISTS_AND_ITEMS = 50;

    private static final String AUTHORITY = "pl.coreorb.shoppinglist.contentprovider";
    private static final String SHOPPING_LIST_BASE_PATH = "shopping_list";
    private static final String ITEM_BASE_PATH = "item";
    private static final String SHOPPING_LIST_AND_ITEM_BASE_PATH = SHOPPING_LIST_BASE_PATH + "_"
            + ITEM_BASE_PATH;

    public static final Uri SHOPPING_LIST_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + SHOPPING_LIST_BASE_PATH);
    public static final String SHOPPING_LIST_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/shopping_lists";
    public static final String SHOPPING_LIST_CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/shopping_list";

    public static final Uri ITEM_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + ITEM_BASE_PATH);
    public static final String ITEM_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/items";
    public static final String ITEM_CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/item";

    public static final Uri SHOPPING_LIST_AND_ITEM_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + SHOPPING_LIST_AND_ITEM_BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, SHOPPING_LIST_BASE_PATH, SHOPPING_LISTS);
        sURIMatcher.addURI(AUTHORITY, SHOPPING_LIST_BASE_PATH + "/#", SHOPPING_LIST_ID);
        sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH, ITEMS);
        sURIMatcher.addURI(AUTHORITY, ITEM_BASE_PATH + "/#", ITEM_ID);
        sURIMatcher.addURI(AUTHORITY, SHOPPING_LIST_AND_ITEM_BASE_PATH, SHOPPING_LISTS_AND_ITEMS);
    }

    @Override
    public boolean onCreate() {
        database = new ShoppingListsDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch(sURIMatcher.match(uri)) {
            case SHOPPING_LISTS:
                checkColumnsShoppingList(projection);
                queryBuilder.setTables(ShoppingListsTable.TABLE_SHOPPING_LISTS);
                break;

            case SHOPPING_LIST_ID:
                checkColumnsShoppingList(projection);
                queryBuilder.setTables(ShoppingListsTable.TABLE_SHOPPING_LISTS);
                queryBuilder.appendWhere(ShoppingListsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;

            case ITEMS:
                checkColumnsItem(projection);
                queryBuilder.setTables(ItemsTable.TABLE_ITEMS);
                break;

            case ITEM_ID:
                checkColumnsItem(projection);
                queryBuilder.setTables(ItemsTable.TABLE_ITEMS);
                queryBuilder.appendWhere(ItemsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;

            case SHOPPING_LISTS_AND_ITEMS:
                checkColumnsShoppingListAndItems(projection);
                queryBuilder.setTables(ShoppingListsTable.TABLE_SHOPPING_LISTS + " LEFT JOIN "
                        + ItemsTable.TABLE_ITEMS + " ON ("
                        + ShoppingListsTable.TABLE_SHOPPING_LISTS + "." + ShoppingListsTable.COLUMN_ID
                        + " = " + ItemsTable.TABLE_ITEMS + "." + ItemsTable.COLUMN_SHOPPING_LIST_ID + ")");
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = database.getWritableDatabase();
        Uri _uri;
        long id;
        switch(sURIMatcher.match(uri)) {
            case SHOPPING_LISTS:
                id = db.insert(ShoppingListsTable.TABLE_SHOPPING_LISTS, null, contentValues);
                _uri = Uri.parse(SHOPPING_LIST_BASE_PATH + "/" + id);
                break;
            case ITEMS:
                id = db.insert(ItemsTable.TABLE_ITEMS, null, contentValues);
                _uri = Uri.parse(ITEM_BASE_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return _uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsDeleted;
        String id;
        switch(sURIMatcher.match(uri)) {
            case SHOPPING_LISTS:
                rowsDeleted = db.delete(ShoppingListsTable.TABLE_SHOPPING_LISTS, selection, selectionArgs);
                break;
            case SHOPPING_LIST_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(ShoppingListsTable.TABLE_SHOPPING_LISTS, ShoppingListsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = db.delete(ShoppingListsTable.TABLE_SHOPPING_LISTS, ShoppingListsTable.COLUMN_ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            case ITEMS:
                rowsDeleted = db.delete(ItemsTable.TABLE_ITEMS, selection, selectionArgs);
                break;
            case ITEM_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(ItemsTable.TABLE_ITEMS,
                            ItemsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(ItemsTable.TABLE_ITEMS,
                            ItemsTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsUpdated;
        String id;
        switch(sURIMatcher.match(uri)) {
            case SHOPPING_LISTS:
                rowsUpdated = db.update(ShoppingListsTable.TABLE_SHOPPING_LISTS, contentValues, selection,
                        selectionArgs);
                break;
            case SHOPPING_LIST_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(ShoppingListsTable.TABLE_SHOPPING_LISTS, contentValues,
                            ShoppingListsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(ShoppingListsTable.TABLE_SHOPPING_LISTS, contentValues,
                            ShoppingListsTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case ITEMS:
                rowsUpdated = db.update(ItemsTable.TABLE_ITEMS, contentValues, selection,
                        selectionArgs);
                break;
            case ITEM_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(ItemsTable.TABLE_ITEMS, contentValues,
                            ItemsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(ItemsTable.TABLE_ITEMS, contentValues,
                            ItemsTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    private void checkColumnsShoppingList(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ShoppingListsTable.COLUMNS_ALL));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private void checkColumnsItem(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ItemsTable.COLUMNS_ALL));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private void checkColumnsShoppingListAndItems(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(ShoppingListsTable.COLUMNS_ALL));
            availableColumns.addAll(Arrays.asList(ItemsTable.COLUMNS_ALL));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
