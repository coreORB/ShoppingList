package pl.coreorb.shoppinglist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import pl.coreorb.shoppinglist.contentprovider.ItemsTable;
import pl.coreorb.shoppinglist.contentprovider.ShoppingListsContentProvider;
import pl.coreorb.shoppinglist.contentprovider.ShoppingListsTable;
import pl.coreorb.shoppinglist.contentprovideraccess.CreateCallback;
import pl.coreorb.shoppinglist.pojos.Item;
import pl.coreorb.shoppinglist.pojos.ShoppingList;
import pl.coreorb.shoppinglist.utils.Logger;

/**
 * Created by ZaYeR on 2016-05-16.
 */
public class TestData {

    private static final String LOG_TAG = TestData.class.getSimpleName();

    /**
     * Inserts test shopping lists to database.
     *
     * @param contentResolver content resolver
     * @param lang language, currently: "en" or "pl"
     * @param callback returns onSuccess when insert finishes.
     */
    public void createTestData(final ContentResolver contentResolver, String lang, final CreateCallback<ShoppingList> callback) {
        new AsyncTask<ShoppingList, Void, Void>() {
            @Override
            protected Void doInBackground(ShoppingList[] params) {
                ContentValues contentValues;
                Uri tempUri;
                for (int i = 0; i < params.length; i++) {
                    contentValues = new ContentValues();
                    contentValues.put(ShoppingListsTable.COLUMN_TITLE, params[i].getTitle());
                    contentValues.put(ShoppingListsTable.COLUMN_CREATED_AT, params[i].getCreatedAt().getTimeInMillis());
                    contentValues.put(ShoppingListsTable.COLUMN_ARCHIVED, params[i].isArchived());

                    long id = -1;
                    tempUri = contentResolver.insert(ShoppingListsContentProvider.SHOPPING_LIST_CONTENT_URI, contentValues);
                    if (tempUri != null) {
                        id = Long.parseLong(tempUri.getLastPathSegment());
                    } else {
                        Logger.e(LOG_TAG, "Failed to insert shopping list: " + params[i]);
                    }

                    for (Item item : params[i].getItems()) {
                        contentValues = new ContentValues();
                        contentValues.put(ItemsTable.COLUMN_SHOPPING_LIST_ID, id);
                        contentValues.put(ItemsTable.COLUMN_CONTENT, item.getContent());
                        contentValues.put(ItemsTable.COLUMN_CHECKED, item.isChecked());
                        contentValues.put(ItemsTable.COLUMN_TIMESTAMP, (new GregorianCalendar()).getTimeInMillis());

                        long idItem = -1;
                        tempUri = contentResolver.insert(ShoppingListsContentProvider.ITEM_CONTENT_URI,
                                contentValues);
                        if (tempUri != null) {
                            idItem = Long.parseLong(tempUri.getLastPathSegment());
                        } else {
                            Logger.e(LOG_TAG, "Failed to insert item: " + item);
                        }
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                callback.onSuccess(new ShoppingList());
            }
        }.execute(getTestArray(lang).toArray(new ShoppingList[0]));
    }

    /**
     * Creates ArrayList of test shopping lists.
     * @param lang language, currently: "en" or "pl"
     * @return test shopping lists
     */
    private ArrayList<ShoppingList> getTestArray(String lang) {
        ArrayList<ShoppingList> result = new ArrayList<>();
        ShoppingList tempShoppingList;
        ArrayList<Item> tempItems;

        if (lang.equalsIgnoreCase("en")) {
            //groceries
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "sugar", false));
            tempItems.add(new Item(-1, -1, "rice", false));
            tempItems.add(new Item(-1, -1, "pie!", true));
            tempItems.add(new Item(-1, -1, "cheese", false));
            tempItems.add(new Item(-1, -1, "lemon", false));
            tempItems.add(new Item(-1, -1, "coffee", true));
            tempItems.add(new Item(-1, -1, "tea", false));
            tempItems.add(new Item(-1, -1, "2 carrots", false));
            tempItems.add(new Item(-1, -1, "potatoes", false));
            tempItems.add(new Item(-1, -1, "bananas", true));
            tempItems.add(new Item(-1, -1, "cookies", false));
            tempItems.add(new Item(-1, -1, "chicken", true));
            tempItems.add(new Item(-1, -1, "3 bagiels", false));
            result.add(new ShoppingList(-1, "Groceries", false, new GregorianCalendar(2016, 0, 10), tempItems, false));

            //PC parts
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "SSD Samsung 850 Evo 120GB", false));
            tempItems.add(new Item(-1, -1, "case SilentiumPC", false));
            tempItems.add(new Item(-1, -1, "motherboard AS-Rock socket LGA1155", false));
            tempItems.add(new Item(-1, -1, "DDR3 8GB RAM", false));
            tempItems.add(new Item(-1, -1, "CPU i5-4500K", false));
            result.add(new ShoppingList(-1, "PC parts", false, new GregorianCalendar(2016, 0, 9), tempItems, false));

            //tools
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "hammer", false));
            tempItems.add(new Item(-1, -1, "hand drill", false));
            tempItems.add(new Item(-1, -1, "nails", false));
            tempItems.add(new Item(-1, -1, "sandpaper", false));
            tempItems.add(new Item(-1, -1, "measure tape", false));
            tempItems.add(new Item(-1, -1, "hand saw", false));
            tempItems.add(new Item(-1, -1, "wrench", false));
            result.add(new ShoppingList(-1, "Tools", false, new GregorianCalendar(2016, 0, 8), tempItems, false));

            //furniture
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "bookcase", false));
            tempItems.add(new Item(-1, -1, "2 computer hairs", false));
            tempItems.add(new Item(-1, -1, "2 computer desks", false));
            tempItems.add(new Item(-1, -1, "dinning table", false));
            tempItems.add(new Item(-1, -1, "4 dinning chairs", false));
            result.add(new ShoppingList(-1, "Furniture", false, new GregorianCalendar(2016, 0, 7), tempItems, false));

            //other
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "text marker", false));
            tempItems.add(new Item(-1, -1, "plastic cups", false));
            tempItems.add(new Item(-1, -1, "6 knifes", false));
            tempItems.add(new Item(-1, -1, "towel", false));
            result.add(new ShoppingList(-1, "Other", false, new GregorianCalendar(2016, 0, 6), tempItems, false));

            //for pet
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "collar", false));
            tempItems.add(new Item(-1, -1, "pet bed", true));
            tempItems.add(new Item(-1, -1, "dog doors", false));
            result.add(new ShoppingList(-1, "For pet", true, new GregorianCalendar(2016, 0, 6), tempItems, false));
        } else if (lang.equalsIgnoreCase("pl")) {
            //groceries
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "cukier", false));
            tempItems.add(new Item(-1, -1, "ryż", false));
            tempItems.add(new Item(-1, -1, "ciasto!", true));
            tempItems.add(new Item(-1, -1, "ser", false));
            tempItems.add(new Item(-1, -1, "cytryna", false));
            tempItems.add(new Item(-1, -1, "kawa", true));
            tempItems.add(new Item(-1, -1, "herbata", false));
            tempItems.add(new Item(-1, -1, "2 marchewki", false));
            tempItems.add(new Item(-1, -1, "ziemniaki", false));
            tempItems.add(new Item(-1, -1, "banany", true));
            tempItems.add(new Item(-1, -1, "ciastka", false));
            tempItems.add(new Item(-1, -1, "kurczak", true));
            tempItems.add(new Item(-1, -1, "3 bagietki", false));
            result.add(new ShoppingList(-1, "Zakupy", false, new GregorianCalendar(2016, 0, 10), tempItems, false));

            //PC parts
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "SSD Samsung 850 Evo 120GB", false));
            tempItems.add(new Item(-1, -1, "obudowa SilentiumPC", false));
            tempItems.add(new Item(-1, -1, "płyta główna AS-Rock socket LGA1155", false));
            tempItems.add(new Item(-1, -1, "DDR3 8GB RAM", false));
            tempItems.add(new Item(-1, -1, "CPU i5-4500K", false));
            result.add(new ShoppingList(-1, "Części do PC", false, new GregorianCalendar(2016, 0, 9), tempItems, false));

            //tools
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "młotek", false));
            tempItems.add(new Item(-1, -1, "wiertarka ręczna", false));
            tempItems.add(new Item(-1, -1, "gwoździe", false));
            tempItems.add(new Item(-1, -1, "papier ścierny", false));
            tempItems.add(new Item(-1, -1, "miarka", false));
            tempItems.add(new Item(-1, -1, "piła ręczna", false));
            tempItems.add(new Item(-1, -1, "klucz francuski", false));
            result.add(new ShoppingList(-1, "Narzędzia", false, new GregorianCalendar(2016, 0, 8), tempItems, false));

            //furniture
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "regał", false));
            tempItems.add(new Item(-1, -1, "2 krzesła komputerowe", false));
            tempItems.add(new Item(-1, -1, "2 biurka", false));
            tempItems.add(new Item(-1, -1, "stół do jadalnii", false));
            tempItems.add(new Item(-1, -1, "4 krzesła do jadalnii", false));
            result.add(new ShoppingList(-1, "Meble", false, new GregorianCalendar(2016, 0, 7), tempItems, false));

            //other
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "zakreślacz", false));
            tempItems.add(new Item(-1, -1, "plastikowe kubki", false));
            tempItems.add(new Item(-1, -1, "6 noży", false));
            tempItems.add(new Item(-1, -1, "ręcznik", false));
            result.add(new ShoppingList(-1, "Inne", false, new GregorianCalendar(2016, 0, 6), tempItems, false));

            //for pet
            tempItems = new ArrayList<>();
            tempItems.add(new Item(-1, -1, "obroża", false));
            tempItems.add(new Item(-1, -1, "łóżko dla psa", true));
            tempItems.add(new Item(-1, -1, "drzwi dla psa", false));
            result.add(new ShoppingList(-1, "Dla zwierzaka", true, new GregorianCalendar(2016, 0, 6), tempItems, false));
        }

        return result;
    }
}
