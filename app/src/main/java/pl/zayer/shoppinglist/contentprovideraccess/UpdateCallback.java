package pl.zayer.shoppinglist.contentprovideraccess;

/**
 * Created by ZaYeR on 2016-05-12.
 */
public interface UpdateCallback<T> {

    public void onSuccess(T object);

    public void onFailure(int errorCode);

}
