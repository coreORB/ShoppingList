package pl.zayer.shoppinglist.activities;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.contentprovideraccess.ContentProviderAccess;
import pl.zayer.shoppinglist.pojos.Item;
import pl.zayer.shoppinglist.pojos.ShoppingList;
import pl.zayer.shoppinglist.contentprovideraccess.CreateCallback;
import pl.zayer.shoppinglist.contentprovideraccess.DeleteCallback;
import pl.zayer.shoppinglist.contentprovideraccess.SelectCallback;
import pl.zayer.shoppinglist.contentprovideraccess.UpdateCallback;

/**
 * Fragment class showing shopping list details and enabling editing if shopping list is not
 * archived.
 */
public class ListDetailsActivityFragment extends Fragment implements
        ListDetailsActivityFragmentListAdapter.OnItemAddedListener,
        ListDetailsActivityFragmentListAdapter.OnItemEditedListener,
        ListDetailsActivityFragmentListAdapter.OnItemRemoveClickedListener,
        ListDetailsActivityFragmentListAdapter.OnShoppingListTitleEditedListener {

    private static final String LOG_TAG = ListDetailsActivityFragment.class.getSimpleName();

    private static final String STATE_CURRENT_SHOPPING_LIST = "state_current_shopping_list";

    private static final String ARG_SHOPPING_LIST = "arg_shopping_list";

    private RecyclerView listRV;
    private ListDetailsActivityFragmentListAdapter adapter;
    private MaterialProgressBar loadingMPB;

    private ContentProviderAccess contentProviderAccess;
    private ShoppingList currentShoppingList;

    private boolean queryRunning = false;

    public static ListDetailsActivityFragment newInstance() {
        ListDetailsActivityFragment fragment = new ListDetailsActivityFragment();
        return fragment;
    }

    public static ListDetailsActivityFragment newInstance(ShoppingList shoppingList) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_SHOPPING_LIST, shoppingList);

        ListDetailsActivityFragment fragment = new ListDetailsActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ListDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_list_details, container, false);

        listRV = (RecyclerView) rootView.findViewById(R.id.list_rv);
        loadingMPB = (MaterialProgressBar) rootView.findViewById(R.id.loading_mpb);

        contentProviderAccess = new ContentProviderAccess(getActivity().getContentResolver());

        showViewLoading();

        Bundle args = getArguments();
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else if (args != null && args.containsKey(ARG_SHOPPING_LIST)) {
            onCreateViewEditList((ShoppingList) args.getParcelable(ARG_SHOPPING_LIST));
        } else {
            onCreateViewNewList();
        }

        return rootView;
    }

    private void onCreateViewNewList() {
        Log.i(LOG_TAG, "onCreateViewNewList()");
        currentShoppingList = new ShoppingList();

        //create new shopping list in database
        CreateCallback<ShoppingList> callback = new CreateCallback<ShoppingList>() {
            @Override
            public void onSuccess(ShoppingList object) {
                object.setFullyLoaded(true);
                setUpAndShowList(object.isArchived(), object.getTitle(), object.getItems());
            }

            @Override
            public void onFailure(int errorCode) {
                Snackbar.make(getView(), R.string.fragment_list_details_error_create_item, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onCreateViewEditList(currentShoppingList);
                            }
                        }).show();
            }
        };
        contentProviderAccess.createShoppingList(currentShoppingList, callback);


    }

    private void onCreateViewEditList(final ShoppingList initialShoppingList) {
        Log.i(LOG_TAG, "onCreateViewEditList(" + initialShoppingList + ")");
        currentShoppingList = initialShoppingList;

        //if object if already fully loaded just read data from object
        if (initialShoppingList.isFullyLoaded()) {
            setUpAndShowList(initialShoppingList.isArchived(),
                    initialShoppingList.getTitle(),
                    currentShoppingList.getItems());
        } else {
            //get items for current shopping list
            SelectCallback<Item> callback = new SelectCallback<Item>() {
                @Override
                public void onSuccess(ArrayList<Item> objects) {
                    currentShoppingList.setItems(objects);
                    currentShoppingList.setFullyLoaded(true);
                    setUpAndShowList(currentShoppingList.isArchived(),
                            currentShoppingList.getTitle(),
                            currentShoppingList.getItems());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int errorCode) {
                    showViewList();
                    Snackbar.make(getView(), R.string.fragment_list_details_error_reading_items, Snackbar.LENGTH_LONG)
                            .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onCreateViewNewList();
                                }
                            }).show();
                }
            };
            contentProviderAccess.getItemsForShoppingList(currentShoppingList, callback);
        }
    }

    private void setUpAndShowList(boolean isArchived, String title, ArrayList<Item> items) {
        adapter = new ListDetailsActivityFragmentListAdapter(ListDetailsActivityFragment.this.getContext(),
                isArchived,
                title,
                items,
                this, this, this, this);
        listRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listRV.setAdapter(adapter);
        showViewList();
    }

    @Override
    public void onItemAdded(final Item item) {
        queryRunning = true;
        CreateCallback<Item> callback = new CreateCallback<Item>() {
            @Override
            public void onSuccess(Item object) {
                currentShoppingList.addItemAtBeginning(object);
                adapter.notifyDataSetChanged();
                Log.i(LOG_TAG, "item added " + item);
                queryRunning = false;
            }

            @Override
            public void onFailure(int errorCode) {
                Snackbar.make(getView(), R.string.fragment_list_details_error_create_item, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onItemAdded(item);
                            }
                        }).show();
                queryRunning = false;
            }
        };
        contentProviderAccess.createItem(item, currentShoppingList.getId(), callback);
    }

    @Override
    public void onItemEdited(final Item item) {
        queryRunning = true;
        UpdateCallback<Item> callback = new UpdateCallback<Item>() {
            @Override
            public void onSuccess(Item object) {
                Log.i(LOG_TAG, "onItemEdited - callback.onSuccess("+item+")");
                currentShoppingList.updateItem(object);
                adapter.notifyDataSetChanged();
                queryRunning = false;
            }

            @Override
            public void onFailure(int errorCode) {
                Snackbar.make(getView(), R.string.fragment_list_details_error_update_item, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onItemEdited(item);
                            }
                        }).show();
                queryRunning = false;
            }
        };
        contentProviderAccess.updateItem(item, callback);
    }

    @Override
    public void onItemRemoveClicked(final Item item) {
        queryRunning = true;
        DeleteCallback<Item> callback = new DeleteCallback<Item>() {
            @Override
            public void onSuccess(Item object) {
                currentShoppingList.removeItem(object);
                adapter.notifyDataSetChanged();
                queryRunning = false;
            }

            @Override
            public void onFailure(int errorCode) {
                Snackbar.make(getView(), R.string.fragment_list_details_error_delete_item, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onItemEdited(item);
                            }
                        }).show();
                queryRunning = false;
            }
        };
        contentProviderAccess.deleteItem(item, callback);
    }

    @Override
    public void onShoppingListTitleEdited(final String title) {
        queryRunning = true;
        UpdateCallback<ShoppingList> callback = new UpdateCallback<ShoppingList>() {
            @Override
            public void onSuccess(ShoppingList object) {
                currentShoppingList.setTitle(title);
                adapter.setShoppingListTitle(title);
                queryRunning = false;
            }

            @Override
            public void onFailure(int errorCode) {
                Snackbar.make(getView(), R.string.fragment_list_details_error_update_shopping_list, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onShoppingListTitleEdited(title);
                            }
                        }).show();
                queryRunning = false;
            }
        };
        contentProviderAccess.updateShoppingList(currentShoppingList, callback);
    }

    public void showViewLoading() {
        loadingMPB.setVisibility(View.VISIBLE);
        listRV.setVisibility(View.GONE);
    }

    private void showViewList() {
        loadingMPB.setVisibility(View.GONE);
        listRV.setVisibility(View.VISIBLE);
    }

    public ShoppingList getCurrentShoppingList() {
        return currentShoppingList;
    }

    public boolean isQueryRunning() {
        return queryRunning;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        currentShoppingList = savedInstanceState.getParcelable(STATE_CURRENT_SHOPPING_LIST);
        setUpAndShowList(currentShoppingList.isArchived(),
                currentShoppingList.getTitle(),
                currentShoppingList.getItems());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_CURRENT_SHOPPING_LIST, currentShoppingList);
        super.onSaveInstanceState(outState);
    }
}
