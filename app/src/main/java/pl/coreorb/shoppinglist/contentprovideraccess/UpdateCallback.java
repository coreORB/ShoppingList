package pl.coreorb.shoppinglist.contentprovideraccess;

/**
 * Callback executed when object is updated in DB.
 */
public interface UpdateCallback<T> {

    void onSuccess(T object);

    void onFailure();

}
