package pl.coreorb.shoppinglist.contentprovideraccess;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import pl.coreorb.shoppinglist.contentprovider.ItemsTable;
import pl.coreorb.shoppinglist.contentprovider.ShoppingListsContentProvider;
import pl.coreorb.shoppinglist.contentprovider.ShoppingListsTable;
import pl.coreorb.shoppinglist.pojos.Item;
import pl.coreorb.shoppinglist.pojos.ShoppingList;
import pl.coreorb.shoppinglist.utils.Logger;

/**
 * Class provides methods for easy access to Content Provider with shopping lists and items.
 * Created by ZaYeR on 2016-05-12.
 */
public class ContentProviderAccess {

    private static final String LOG_TAG = ContentProviderAccess.class.getSimpleName();

    private final ContentResolver mContentResolver;

    public ContentProviderAccess(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    /**
     * Fetches all shopping lists (WITHOUT items) and sort them by creation date (descending).
     * If operation completes without errors callback will return data in onSuccess method,
     * otherwise onFailure will be called.
     * @param callback callback to return data to
     */
    @SuppressWarnings("unused")
    public void getShoppingLists(final SelectCallback<ShoppingList> callback) {
        Logger.v(LOG_TAG, "getShoppingLists()");
        new GetShoppingListsTask(mContentResolver, callback).execute();
    }

    /**
     * Fetches selected type of shopping lists (with unchecked items) and sort them by creation date (descending).
     * If operation completes without errors callback will return data in onSuccess method,
     * otherwise onFailure will be called.
     *
     * @param archived if true archived shopping lists will be fetched, otherwise not archived
     * @param callback callback to return data to
     */
    public void getShoppingListsAndUncheckedItems(final boolean archived, final SelectCallback<ShoppingList> callback) {
        Logger.v(LOG_TAG, "getShoppingListsAndUncheckedItems()");
        new GetShoppingListsAndUncheckedItemsTask(mContentResolver, archived, callback).execute();
    }

    /**
     * Creates new shopping list (WITHOUT items). If operation completes without errors callback
     * will return created object with filled id in onSuccess method, otherwise onFailure will be called.
     *
     * @param shoppingList object to create
     * @param callback     callback to return data to
     */
    public void createShoppingList(final ShoppingList shoppingList, final CreateCallback<ShoppingList> callback) {
        Logger.v(LOG_TAG, "createShoppingList(" + shoppingList + ")");
        new CreateShoppingListTask(mContentResolver, shoppingList, callback).execute();
    }

    /**
     * Updates provided shopping list (WITHOUT items). If operation completes without errors callback
     * will return updated data in onSuccess method, otherwise onFailure will be called.
     *
     * @param shoppingList object to update
     * @param callback     callback to return data to
     */
    public void updateShoppingList(final ShoppingList shoppingList, final UpdateCallback<ShoppingList> callback) {
        Logger.v(LOG_TAG, "updateShoppingList(" + shoppingList + ")");
        new UpdateShoppingListTask(mContentResolver, shoppingList, callback).execute();
    }

    /**
     * Deletes provided shopping list and all items in database assigned to it. If operation
     * completes without errors callback will return deleted object in onSuccess method, otherwise
     * onFailure will be called.
     *
     * @param shoppingList object to delete
     * @param callback     callback to return data to
     */
    public void deleteShoppingList(final ShoppingList shoppingList, final DeleteCallback<ShoppingList> callback) {
        Logger.v(LOG_TAG, "deleteShoppingList(" + shoppingList + ")");
        new DeleteShoppingListTask(mContentResolver, shoppingList, callback).execute();
    }

    /**
     * Fetches all items for chosen shopping list. If operation completes without errors callback
     * will return data in onSuccess method, otherwise onFailure will be called.
     *
     * @param shoppingList shopping list, for which data will be fetched
     * @param callback     callback to return data to
     */
    public void getItemsForShoppingList(final ShoppingList shoppingList, final SelectCallback<Item> callback) {
        Logger.v(LOG_TAG, "getItemsForShoppingList(" + shoppingList + ")");
        new GetItemsForShoppingList(mContentResolver, shoppingList, callback).execute();
    }

    /**
     * Creates new item of shopping list. If operation completes without errors callback
     * will return created object with filled id in onSuccess method, otherwise onFailure will be called.
     *
     * @param item                 object to create
     * @param parentShoppingListId id of shopping list, for which item will be created
     * @param callback             callback to return data to
     */
    public void createItem(final Item item, final long parentShoppingListId, final CreateCallback<Item> callback) {
        Logger.v(LOG_TAG, "createItem(" + item + ", " + parentShoppingListId + ")");
        new CreateItemTask(mContentResolver, item, parentShoppingListId, callback).execute();
    }

    /**
     * Updates provided item of shopping list. If operation completes without errors callback
     * will return updated data in onSuccess method, otherwise onFailure will be called.
     * There is not possible to move items to another shopping list at the time.
     *
     * @param item     object to update
     * @param callback callback to return data to
     */
    public void updateItem(final Item item, final UpdateCallback<Item> callback) {
        Logger.v(LOG_TAG, "updateItem(" + item + ")");
        new UpdateItemTask(mContentResolver, item, callback).execute();
    }

    /**
     * Deletes provided item of shopping list. If operation completes without errors callback will return
     * deleted object in onSuccess method, otherwise onFailure will be called.
     *
     * @param item     object to delete
     * @param callback callback to return data to
     */
    public void deleteItem(final Item item, final DeleteCallback<Item> callback) {
        Logger.v(LOG_TAG, "deleteItem(" + item + ")");
        new DeleteItemTask(mContentResolver, item, callback).execute();
    }

    private static class GetShoppingListsTask extends AsyncTask<Void, Void, ArrayList<ShoppingList>> {

        private final WeakReference<ContentResolver> cr;
        private final SelectCallback<ShoppingList> callback;

        GetShoppingListsTask(ContentResolver contentResolver, SelectCallback<ShoppingList> callback) {
            cr = new WeakReference<>(contentResolver);
            this.callback = callback;
        }

        @Override
        protected ArrayList<ShoppingList> doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            Cursor cursor = mContentResolver.query(
                    ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                    new String[]{ShoppingListsTable.COLUMN_ID, ShoppingListsTable.COLUMN_TITLE,
                            ShoppingListsTable.COLUMN_ARCHIVED,
                            ShoppingListsTable.COLUMN_CREATED_AT},
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
                callback.onFailure();
            } else {
                callback.onSuccess(result);
            }
        }

        /**
         * Converts Cursor object to ShoppingList object.
         *
         * @param cursor cursor with active row
         * @return ready ShoppingList object
         */
        private ShoppingList cursorToShoppingList(Cursor cursor) {
            ShoppingList item = new ShoppingList();
            item.setId(cursor.getLong(cursor.getColumnIndex(ShoppingListsTable.COLUMN_ID)));
            item.setTitle(cursor.getString(cursor.getColumnIndex(ShoppingListsTable.COLUMN_TITLE)));
            item.setArchived(cursor.getShort(cursor.getColumnIndex(ShoppingListsTable.COLUMN_ARCHIVED)) == 1);
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ShoppingListsTable.COLUMN_CREATED_AT)));
            item.setCreatedAt(cal);
            return item;
        }
    }

    private static class GetShoppingListsAndUncheckedItemsTask extends AsyncTask<Void, Void, ArrayList<ShoppingList>> {

        private final WeakReference<ContentResolver> cr;
        private final SelectCallback<ShoppingList> callback;
        private final boolean archived;

        GetShoppingListsAndUncheckedItemsTask(ContentResolver contentResolver, boolean archived, SelectCallback<ShoppingList> callback) {
            cr = new WeakReference<>(contentResolver);
            this.archived = archived;
            this.callback = callback;
        }

        @Override
        protected ArrayList<ShoppingList> doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            Cursor cursor = mContentResolver.query(
                    ShoppingListsContentProvider.SHOPPING_LIST_AND_ITEM_CONTENT_URI,
                    new String[]{
                            ShoppingListsTable.TABLE_SHOPPING_LISTS + "." + ShoppingListsTable.COLUMN_ID,
                            ShoppingListsTable.COLUMN_TITLE,
                            ShoppingListsTable.COLUMN_CREATED_AT,
                            ItemsTable.TABLE_ITEMS + "." + ItemsTable.COLUMN_ID,
                            ItemsTable.COLUMN_CONTENT},
                    ShoppingListsTable.COLUMN_ARCHIVED + " = ? AND ("
                            + ItemsTable.COLUMN_CHECKED + " IS null OR " + ItemsTable.COLUMN_CHECKED + " != ?)",
                    new String[]{archived ? "1" : "0", "1"},
                    ShoppingListsTable.COLUMN_CREATED_AT + " DESC, " + ItemsTable.COLUMN_TIMESTAMP + " DESC"
            );

            if (cursor == null) {
                return null;
            }

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
                callback.onFailure();
            } else {
                callback.onSuccess(result);
            }
        }
    }

    private static class CreateShoppingListTask extends AsyncTask<Void, Void, Long> {

        private final WeakReference<ContentResolver> cr;
        private final CreateCallback<ShoppingList> callback;
        private final ShoppingList shoppingList;

        CreateShoppingListTask(ContentResolver contentResolver, ShoppingList shoppingList, CreateCallback<ShoppingList> callback) {
            cr = new WeakReference<>(contentResolver);
            this.shoppingList = shoppingList;
            this.callback = callback;
        }

        @Override
        protected Long doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ShoppingListsTable.COLUMN_TITLE, shoppingList.getTitle());
            contentValues.put(ShoppingListsTable.COLUMN_CREATED_AT, shoppingList.getCreatedAt().getTimeInMillis());
            contentValues.put(ShoppingListsTable.COLUMN_ARCHIVED, shoppingList.isArchived());

            long id = -1;
            Uri tempUri = mContentResolver.insert(ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI, contentValues);
            if (tempUri != null) {
                id = Long.parseLong(tempUri.getLastPathSegment());
            }
            return id;
        }

        protected void onPostExecute(Long result) {
            if (result == -1) {
                callback.onFailure();
            } else {
                shoppingList.setId(result);
                callback.onSuccess(shoppingList);
            }
        }
    }

    private static class UpdateShoppingListTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<ContentResolver> cr;
        private final UpdateCallback<ShoppingList> callback;
        private final ShoppingList shoppingList;

        UpdateShoppingListTask(ContentResolver contentResolver, ShoppingList shoppingList, UpdateCallback<ShoppingList> callback) {
            cr = new WeakReference<>(contentResolver);
            this.shoppingList = shoppingList;
            this.callback = callback;
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ShoppingListsTable.COLUMN_TITLE, shoppingList.getTitle());
            contentValues.put(ShoppingListsTable.COLUMN_ARCHIVED, shoppingList.isArchived());

            return mContentResolver.update(
                    ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                    contentValues,
                    ShoppingListsTable.COLUMN_ID + " = ?",
                    new String[]{shoppingList.getId() + ""}
            );
        }

        protected void onPostExecute(Integer result) {
            if (result == 1) {
                callback.onSuccess(shoppingList);
            } else {
                callback.onFailure();
            }
        }
    }

    private static class DeleteShoppingListTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<ContentResolver> cr;
        private final DeleteCallback<ShoppingList> callback;
        private final ShoppingList shoppingList;

        DeleteShoppingListTask(ContentResolver contentResolver, ShoppingList shoppingList, DeleteCallback<ShoppingList> callback) {
            cr = new WeakReference<>(contentResolver);
            this.shoppingList = shoppingList;
            this.callback = callback;
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            mContentResolver.delete(
                    ShoppingListsContentProvider.ITEM_CONTENT_URI,
                    ItemsTable.COLUMN_SHOPPING_LIST_ID + " = ?",
                    new String[]{shoppingList.getId() + ""}
            );

            return mContentResolver.delete(
                    ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI,
                    ShoppingListsTable.COLUMN_ID + " = ?",
                    new String[]{shoppingList.getId() + ""}
            );
        }

        protected void onPostExecute(Integer result) {
            if (result == 1) {
                callback.onSuccess(shoppingList);
            } else {
                callback.onFailure();
            }
        }
    }

    private static class GetItemsForShoppingList extends AsyncTask<Void, Void, ArrayList<Item>> {

        private final WeakReference<ContentResolver> cr;
        private final SelectCallback<Item> callback;
        private ShoppingList shoppingList;

        GetItemsForShoppingList(ContentResolver contentResolver, ShoppingList shoppingList, SelectCallback<Item> callback) {
            cr = new WeakReference<>(contentResolver);
            this.shoppingList = shoppingList;
            this.callback = callback;
        }

        @Override
        protected ArrayList<Item> doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            long shoppingListId = shoppingList.getId();

            Cursor cursor = mContentResolver.query(
                    ShoppingListsContentProvider.ITEM_CONTENT_URI,
                    new String[]{ItemsTable.COLUMN_ID, ItemsTable.COLUMN_CONTENT,
                            ItemsTable.COLUMN_CHECKED},
                    ItemsTable.COLUMN_SHOPPING_LIST_ID + " = ?",
                    new String[]{shoppingListId + ""},
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
                    result.add(cursorToItem(cursor));
                }
            }
            cursor.close();
            return result;
        }

        protected void onPostExecute(ArrayList<Item> result) {
            if (result == null) {
                callback.onFailure();
            } else {
                callback.onSuccess(result);
            }
        }

        /**
         * Converts Cursor object to Item object.
         *
         * @param cursor cursor with active row
         * @return ready Item object
         */
        private Item cursorToItem(Cursor cursor) {
            Item item = new Item();
            item.setId(cursor.getLong(cursor.getColumnIndex(ItemsTable.COLUMN_ID)));
            item.setContent(cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_CONTENT)));
            item.setChecked(cursor.getShort(cursor.getColumnIndex(ItemsTable.COLUMN_CHECKED)) == 1);
            return item;
        }
    }

    private static class CreateItemTask extends AsyncTask<Void, Void, Long> {

        private final WeakReference<ContentResolver> cr;
        private final CreateCallback<Item> callback;
        private final Item item;
        private final long parentShoppingListId;

        CreateItemTask(ContentResolver contentResolver, Item item, long parentShoppingListId, CreateCallback<Item> callback) {
            cr = new WeakReference<>(contentResolver);
            this.callback = callback;
            this.parentShoppingListId = parentShoppingListId;
            this.item = item;
        }

        @Override
        protected Long doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ItemsTable.COLUMN_SHOPPING_LIST_ID, parentShoppingListId);
            contentValues.put(ItemsTable.COLUMN_CONTENT, item.getContent());
            contentValues.put(ItemsTable.COLUMN_CHECKED, item.isChecked());
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
                callback.onFailure();
            } else {
                item.setId(result);
                callback.onSuccess(item);
            }
        }
    }

    private static class UpdateItemTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<ContentResolver> cr;
        private final UpdateCallback<Item> callback;
        private final Item item;

        UpdateItemTask(ContentResolver contentResolver, Item item, UpdateCallback<Item> callback) {
            cr = new WeakReference<>(contentResolver);
            this.callback = callback;
            this.item = item;
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ItemsTable.COLUMN_CONTENT, item.getContent());
            contentValues.put(ItemsTable.COLUMN_CHECKED, item.isChecked());

            return mContentResolver.update(
                    ShoppingListsContentProvider.ITEM_CONTENT_URI,
                    contentValues,
                    ItemsTable.COLUMN_ID + " = ?",
                    new String[]{item.getId() + ""});
        }

        protected void onPostExecute(Integer result) {
            if (result == 1) {
                callback.onSuccess(item);
            } else {
                callback.onFailure();
            }
        }
    }

    private static class DeleteItemTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<ContentResolver> cr;
        private final DeleteCallback<Item> callback;
        private final Item item;

        DeleteItemTask(ContentResolver contentResolver, Item item, DeleteCallback<Item> callback) {
            cr = new WeakReference<>(contentResolver);
            this.callback = callback;
            this.item = item;
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            ContentResolver mContentResolver = cr.get();
            if (mContentResolver == null) return null;

            return mContentResolver.delete(
                    ShoppingListsContentProvider.ITEM_CONTENT_URI,
                    ItemsTable.COLUMN_ID + " = ?",
                    new String[]{item.getId() + ""}
            );
        }

        protected void onPostExecute(Integer result) {
            if (result == 1) {
                callback.onSuccess(item);
            } else {
                callback.onFailure();
            }
        }
    }
}
