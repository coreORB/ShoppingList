package pl.coreorb.shoppinglist.contentprovideraccess;

import java.util.ArrayList;

/**
 * Callback executed when object is selected from DB.
 */
public interface SelectCallback<T> {

    void onSuccess(ArrayList<T> objects);

    void onFailure();

}
