package pl.coreorb.shoppinglist.contentprovideraccess;

/**
 * Callback executed when object is deleted from DB.
 */
public interface DeleteCallback<T> {

    void onSuccess(T object);

    void onFailure();

}
