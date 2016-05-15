package pl.zayer.shoppinglist.contentprovideraccess;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import pl.zayer.shoppinglist.contentprovider.ItemsTable;
import pl.zayer.shoppinglist.contentprovider.ShoppingListsContentProvider;
import pl.zayer.shoppinglist.contentprovider.ShoppingListsTable;
import pl.zayer.shoppinglist.pojos.Item;
import pl.zayer.shoppinglist.pojos.ShoppingList;

/**
 * Class provides methods for easy access to Content Provider with shopping lists and items.
 * Created by ZaYeR on 2016-05-12.
 */
public class ContentProviderAccess {

    private static final String LOG_TAG = ContentProviderAccess.class.getSimpleName();

    private ContentResolver mContentResolver;

    public ContentProviderAccess(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    /**
     * Fetches all shopping lists (WITHOUT items) and sort them by creation date (descending).
     * If operation completes without errors callback will return data in onSuccess method,
     * otherwise onFailure will be called.
     * @param callback callback to return data to
     */
    public void getShoppingLists(final SelectCallback<ShoppingList> callback) {
        Log.i(LOG_TAG, "getShoppingLists()");
        new AsyncTask<Void, Void, ArrayList<ShoppingList>>() {
            @Override
            protected ArrayList<ShoppingList> doInBackground(Void[] params) {
                Cursor cursor = mContentResolver.query(
                        ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                        new String[] {ShoppingListsTable.COLUMN_ID, ShoppingListsTable.COLUMN_TITLE,
                                ShoppingListsTable.COLUMN_ARCHIVED,
                                ShoppingListsTable.COLUMN_CREATED_AT },
                        null,
                        null,
                        ShoppingListsTable.COLUMN_CREATED_AT + " DESC"
                );

                if (cursor == null) {
                    return null;
                }

                ArrayList<ShoppingList> result;
                if (cursor.getCount() == 0) {
                    result = new ArrayList<>();
                } else {
                    result = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        result.add(cursorToShoppingList(cursor));
                    }
                }
                cursor.close();
                return result;
            }

            protected void onPostExecute(ArrayList<ShoppingList> result) {
                if (result == null) {
                    callback.onFailure(-1);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    /**
     * Fetches selected type of shopping lists (with unchecked items) and sort them by creation date (descending).
     * If operation completes without errors callback will return data in onSuccess method,
     * otherwise onFailure will be called.
     * @param archived if true archived shopping lists will be fetched, otherwise not archived
     * @param callback callback to return data to
     */
    public void getShoppingListsAndUncheckedItems(final boolean archived, final SelectCallback<ShoppingList> callback) {
        Log.i(LOG_TAG, "getShoppingListsAndUncheckedItems()");
        new AsyncTask<Void, Void, ArrayList<ShoppingList>>() {
            @Override
            protected ArrayList<ShoppingList> doInBackground(Void[] params) {
                Cursor cursor = mContentResolver.query(
                        ShoppingListsContentProvider.SHOPPING_LIST_AND_ITEM_CONTENT_URI,
                        new String[] {
                                ShoppingListsTable.TABLE_SHOPPING_LISTS + "." +ShoppingListsTable.COLUMN_ID,
                                ShoppingListsTable.COLUMN_TITLE,
                                ShoppingListsTable.COLUMN_CREATED_AT,
                                ItemsTable.TABLE_ITEMS + "." + ItemsTable.COLUMN_ID,
                                ItemsTable.COLUMN_CONTENT },
                        ShoppingListsTable.COLUMN_ARCHIVED + " = ? AND ("
                                +ItemsTable.COLUMN_CHECKED + " IS null OR " + ItemsTable.COLUMN_CHECKED + " != ?)",
                        new String[] { archived ? "1" : "0", "1" },
                        ShoppingListsTable.COLUMN_CREATED_AT + " DESC, " + ItemsTable.COLUMN_TIMESTAMP + " DESC"
                );

                if (cursor == null) {
                    return null;
                }

                Log.i(LOG_TAG, "cursor size " + cursor.getCount());
                ArrayList<ShoppingList> result;

                if (cursor.getCount() == 0) {
                    result = new ArrayList<>();
                } else {
                    result = new ArrayList<>();
                    ShoppingList tempShoppingList;
                    Item tempItem;

                    while (cursor.moveToNext()) {
                        //create temporary shopping list object
                        tempShoppingList = new ShoppingList();
                        tempShoppingList.setId(cursor.getLong(0));
                        tempShoppingList.setTitle(cursor.getString(cursor.getColumnIndex(ShoppingListsTable.COLUMN_TITLE)));
                        Log.i(LOG_TAG, "title ----- " + cursor.getString(cursor.getColumnIndex(ShoppingListsTable.COLUMN_TITLE)));
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ShoppingListsTable.COLUMN_CREATED_AT)));
                        tempShoppingList.setCreatedAt(cal);
                        tempShoppingList.setArchived(archived);

                        //create temporary item object
                        if (cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_CONTENT)) == null
                                && cursor.getString(3) == null) {
                            tempItem = null;
                        } else {
                            tempItem = new Item();
                            tempItem.setId(cursor.getLong(3));
                            tempItem.setContent(cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_CONTENT)));
                            tempItem.setChecked(false);
                        }

                        //add objects to result
                        if (result.contains(tempShoppingList) && tempItem != null) {
                            result.get(result.indexOf(tempShoppingList)).addItem(tempItem);
                        } else {
                            if (tempItem != null) {
                                tempShoppingList.addItem(tempItem);
                            }
                            result.add(tempShoppingList);
                        }
                    }
                }
                cursor.close();
                return result;
            }

            protected void onPostExecute(ArrayList<ShoppingList> result) {
                if (result == null) {
                    callback.onFailure(-1);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    /**
     * Converts Cursor object to ShoppingList object.
     * @param cursor cursor with active row
     * @return ready ShoppingList object
     */
    private ShoppingList cursorToShoppingList(Cursor cursor) {
        ShoppingList item = new ShoppingList();
        item.setId(cursor.getLong(cursor.getColumnIndex(ShoppingListsTable.COLUMN_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndex(ShoppingListsTable.COLUMN_TITLE)));
        item.setArchived(cursor.getShort(cursor.getColumnIndex(ShoppingListsTable.COLUMN_ARCHIVED)) == 1 ? true : false);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ShoppingListsTable.COLUMN_CREATED_AT)));
        item.setCreatedAt(cal);
        return item;
    }

    /**
     * Creates new shopping list (WITHOUT items). If operation completes without errors callback
     * will return created object with filled id in onSuccess method, otherwise onFailure will be called.
     * @param shoppingList object to create
     * @param callback callback to return data to
     */
    public void createShoppingList(final ShoppingList shoppingList, final CreateCallback<ShoppingList> callback) {
        Log.i(LOG_TAG, "createShoppingList("+shoppingList+")");
        new AsyncTask<ShoppingList, Void, Long>() {
            @Override
            protected Long doInBackground(ShoppingList[] params) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ShoppingListsTable.COLUMN_TITLE, params[0].getTitle());
                contentValues.put(ShoppingListsTable.COLUMN_CREATED_AT, params[0].getCreatedAt().getTimeInMillis());
                contentValues.put(ShoppingListsTable.COLUMN_ARCHIVED, params[0].isArchived());

                long id = -1;
                Uri tempUri = mContentResolver.insert(ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI, contentValues);
                if (tempUri != null) {
                    id = Long.parseLong(tempUri.getLastPathSegment());
                }
                return id;
            }

            protected void onPostExecute(Long result) {
                if (result == -1) {
                    callback.onFailure(-1);
                } else {
                    shoppingList.setId(result);
                    callback.onSuccess(shoppingList);
                }
            }
        }.execute(shoppingList);
    }

    /**
     * Updates provided shopping list (WITHOUT items). If operation completes without errors callback
     * will return updated data in onSuccess method, otherwise onFailure will be called.
     * @param shoppingList object to update
     * @param callback callback to return data to
     */
    public void updateShoppingList(final ShoppingList shoppingList, final UpdateCallback<ShoppingList> callback) {
        Log.i(LOG_TAG, "updateShoppingList("+shoppingList+")");
        new AsyncTask<ShoppingList, Void, Integer>() {
            @Override
            protected Integer doInBackground(ShoppingList[] params) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ShoppingListsTable.COLUMN_TITLE, params[0].getTitle());
                contentValues.put(ShoppingListsTable.COLUMN_ARCHIVED, params[0].isArchived());

                int rowsUpdated = mContentResolver.update(
                        ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                        contentValues,
                        ShoppingListsTable.COLUMN_ID + " = ?",
                        new String[] { params[0].getId() + "" }
                );

                return rowsUpdated;
            }

            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    callback.onSuccess(shoppingList);
                } else {
                    callback.onFailure(-1);
                }
            }
        }.execute(shoppingList);
    }

    /**
     * Deletes provided shopping list and all items in database assigned to it. If operation
     * completes without errors callback will return deleted object in onSuccess method, otherwise
     * onFailure will be called.
     * @param shoppingList object to delete
     * @param callback callback to return data to
     */
    public void deleteShoppingList(final ShoppingList shoppingList, final DeleteCallback<ShoppingList> callback) {
        Log.i(LOG_TAG, "deleteShoppingList("+shoppingList+")");
        new AsyncTask<ShoppingList, Void, Integer>() {
            @Override
            protected Integer doInBackground(ShoppingList[] params) {
                mContentResolver.delete(
                        ShoppingListsContentProvider.ITEM_CONTENT_URI,
                        ItemsTable.COLUMN_SHOPPING_LIST_ID + " = ?",
                        new String[] { params[0].getId() + "" }
                );

                int deletedRowsCount = mContentResolver.delete(
                        ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                        ShoppingListsTable.COLUMN_ID + " = ?",
                        new String[] { params[0].getId() + "" }
                );

                return deletedRowsCount;
            }

            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    callback.onSuccess(shoppingList);
                } else {
                    callback.onFailure(-1);
                }
            }
        }.execute(shoppingList);
    }

    /**
     * Fetches all items for chosen shopping list. If operation completes without errors callback
     * will return data in onSuccess method, otherwise onFailure will be called.
     * @param shoppingList shopping list, for which data will be fetched
     * @param callback callback to return data to
     */
    public void getItemsForShoppingList(final ShoppingList shoppingList, final SelectCallback<Item> callback) {
        Log.i(LOG_TAG, "getItemsForShoppingList("+shoppingList+")");
        new AsyncTask<ShoppingList, Void, ArrayList<Item>>() {
            @Override
            protected ArrayList<Item> doInBackground(ShoppingList[] params) {
                long shoppingListId = params[0].getId();

                Cursor cursor = mContentResolver.query(
                        ShoppingListsContentProvider.ITEM_CONTENT_URI,
                        new String[] { ItemsTable.COLUMN_ID, ItemsTable.COLUMN_CONTENT,
                                ItemsTable.COLUMN_CHECKED },
                        ItemsTable.COLUMN_SHOPPING_LIST_ID + " = ?",
                        new String[] { shoppingListId + "" },
                        ItemsTable.COLUMN_TIMESTAMP + " DESC"
                );

                if (cursor == null) {
                    return null;
                }

                ArrayList<Item> result;
                if (cursor.getCount() == 0) {
                    result = new ArrayList<>();
                } else {
                    result = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        result.add(cursorToItem(cursor, shoppingListId));
                    }
                }
                cursor.close();
                return result;
            }

            protected void onPostExecute(ArrayList<Item> result) {
                if (result == null) {
                    callback.onFailure(-1);
                } else {
                    callback.onSuccess(result);
                }
            }
        }.execute(shoppingList);
    }

    /**
     * Converts Cursor object to Item object.
     * @param cursor cursor with active row
     * @param shoppingListId id of shopping list, to which item belongs to
     * @return ready Item object
     */
    private Item cursorToItem(Cursor cursor, long shoppingListId) {
        Item item = new Item();
        item.setId(cursor.getLong(cursor.getColumnIndex(ItemsTable.COLUMN_ID)));
        item.setContent(cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_CONTENT)));
        item.setChecked(cursor.getShort(cursor.getColumnIndex(ItemsTable.COLUMN_CHECKED)) == 1 ? true : false);
        return item;
    }

    /**
     * Creates new item of shopping list. If operation completes without errors callback
     * will return created object with filled id in onSuccess method, otherwise onFailure will be called.
     * @param item object to create
     * @param parentShoppingListId id of shopping list, for which item will be created
     * @param callback callback to return data to
     */
    public void createItem(final Item item, final long parentShoppingListId, final CreateCallback<Item> callback) {
        Log.i(LOG_TAG, "createItem("+item+", "+parentShoppingListId+")");
        new AsyncTask<Item, Void, Long>() {
            @Override
            protected Long doInBackground(Item[] params) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ItemsTable.COLUMN_SHOPPING_LIST_ID, parentShoppingListId);
                contentValues.put(ItemsTable.COLUMN_CONTENT, params[0].getContent());
                contentValues.put(ItemsTable.COLUMN_CHECKED, params[0].isChecked());
                contentValues.put(ItemsTable.COLUMN_TIMESTAMP, (new GregorianCalendar()).getTimeInMillis());

                long id = -1;
                Uri tempUri = mContentResolver.insert(ShoppingListsContentProvider.ITEM_CONTENT_URI,
                        contentValues);
                if (tempUri != null) {
                    id = Long.parseLong(tempUri.getLastPathSegment());
                }
                return id;
            }

            protected void onPostExecute(Long result) {
                if (result == -1) {
                    callback.onFailure(-1);
                } else {
                    item.setId(result);
                    callback.onSuccess(item);
                }
            }
        }.execute(item);
    }

    /**
     * Updates provided item of shopping list. If operation completes without errors callback
     * will return updated data in onSuccess method, otherwise onFailure will be called.
     * There is not possible to move items to another shopping list at the time.
     * @param item object to update
     * @param callback callback to return data to
     */
    public void updateItem(final Item item, final UpdateCallback<Item> callback) {
        Log.i(LOG_TAG, "updateItem("+item+")");
        new AsyncTask<Item, Void, Integer>() {
            @Override
            protected Integer doInBackground(Item[] params) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ItemsTable.COLUMN_CONTENT, params[0].getContent());
                contentValues.put(ItemsTable.COLUMN_CHECKED, params[0].isChecked());

                int rowsUpdated = mContentResolver.update(
                        ShoppingListsContentProvider.ITEM_CONTENT_URI,
                        contentValues,
                        ItemsTable.COLUMN_ID + " = ?",
                        new String[] { params[0].getId() + "" } );

                return rowsUpdated;
            }

            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    callback.onSuccess(item);
                } else {
                    callback.onFailure(-1);
                }
            }
        }.execute(item);
    }

    /**
     * Deletes provided item of shopping list. If operation completes without errors callback will return
     * deleted object in onSuccess method, otherwise onFailure will be called.
     * @param item object to delete
     * @param callback callback to return data to
     */
    public void deleteItem(final Item item, final DeleteCallback<Item> callback) {
        Log.i(LOG_TAG, "deleteItem("+item+")");
        new AsyncTask<Item, Void, Integer>() {
            @Override
            protected Integer doInBackground(Item[] params) {
                int deletedRowsCount = mContentResolver.delete(
                        ShoppingListsContentProvider.ITEM_CONTENT_URI,
                        ItemsTable.COLUMN_ID + " = ?",
                        new String[] { params[0].getId() + "" }
                );

                return deletedRowsCount;
            }

            protected void onPostExecute(Integer result) {
                if (result == 1) {
                    callback.onSuccess(item);
                } else {
                    callback.onFailure(-1);
                }
            }
        }.execute(item);
    }


}
