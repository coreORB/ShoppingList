package pl.coreorb.shoppinglist.contentprovideraccess;

/**
 * Callback executed when object is created in DB.
 */
public interface CreateCallback<T> {

    void onSuccess(T object);

    void onFailure();

}
