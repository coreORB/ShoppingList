package pl.zayer.shoppinglist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.contentprovideraccess.ContentProviderAccess;
import pl.zayer.shoppinglist.contentprovideraccess.SelectCallback;
import pl.zayer.shoppinglist.pojos.ShoppingList;

/**
 * Fragment class showing active or archived shopping lists.
 */
public class MainActivityFragment extends Fragment implements MainActivityFragmentListAdapter.OnShoppingListClickedListener {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final String STATE_SHOPPING_LISTS = "state_shopping_lists";

    public static final int REQUEST_CODE_LIST_DETAILS = 5001;

    private RecyclerView listRV;
    private MainActivityFragmentListAdapter adapter;
    private MaterialProgressBar loadingMPB;
    private LinearLayout noListsLL;

    private ArrayList<ShoppingList> currentShoppingLists;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listRV = (RecyclerView) rootView.findViewById(R.id.list_rv);
        loadingMPB = (MaterialProgressBar) rootView.findViewById(R.id.loading_mpb);
        noListsLL = (LinearLayout) rootView.findViewById(R.id.no_lists_ll);

        //set up list
        adapter = new MainActivityFragmentListAdapter(getContext(), this);
        listRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listRV.setAdapter(adapter);

        showViewLoading();

        if (savedInstanceState == null) {
            loadShoppingLists(false);
        } else {
            restoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    /**
     * Shows loading view and loads from database list of shopping lists (archived or active).
     * @param archived wherever to show archived shopping list or not
     */
    public void setShownShoppingListsToArchived(boolean archived) {
        showViewLoading();
        loadShoppingLists(archived);
    }

    /**
     * Loads from database shopping lists and displays them in RecyclerView.
     * @param archived wherever to load archived shopping list or not
     */
    private void loadShoppingLists(final boolean archived) {
        Log.i(LOG_TAG, "loadShoppingLists("+archived+")");
        ContentProviderAccess cpa = new ContentProviderAccess(getActivity().getContentResolver());
        SelectCallback<ShoppingList> callback = new SelectCallback<ShoppingList>() {
            @Override
            public void onSuccess(ArrayList<ShoppingList> objects) {
                showViewList(objects);
            }

            @Override
            public void onFailure(int errorCode) {
                showViewList(null);
                Snackbar.make(getView(), R.string.fragment_main_error_message_reading_shopping_lists, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadShoppingLists(archived);
                            }
                        }).show();
            }
        };
        cpa.getShoppingListsAndUncheckedItems(archived, callback);
    }

    /**
     * Starts ListDetailsActivity to create new shopping list.
     */
    public void startNewListActivity() {
        Intent intent = new Intent(getContext(), ListDetailsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LIST_DETAILS);
    }

    /**
     * Starts ListDetailsActivity to edit selected shopping list.
     * @param shoppingList shopping list selected
     */
    @Override
    public void onShoppingListClicked(ShoppingList shoppingList) {
        Intent intent = new Intent(MainActivityFragment.this.getContext(), ListDetailsActivity.class);
        intent.putExtra(ListDetailsActivity.ARG_SHOPPING_LIST, shoppingList);
        startActivityForResult(intent, REQUEST_CODE_LIST_DETAILS);
    }

    /**
     * Chandles result of ListDetailsActivity and updates shown shopping lists RecyclerView to
     * reflect changes made during creating/editing shopping list.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "onActivityResult()");
        if (requestCode == REQUEST_CODE_LIST_DETAILS && resultCode == Activity.RESULT_OK) {
            ShoppingList shoppingList = data.getParcelableExtra(ListDetailsActivity.RESULT_SHOPPING_LIST);
            int operation = data.getIntExtra(ListDetailsActivity.RESULT_OPERATION, -1);
            switch (operation) {
                case ListDetailsActivity.OPERATION_ADD:
                    currentShoppingLists.add(0, shoppingList);
                    adapter.notifyDataSetChanged();
                    if (adapter.getItemCount() == 1) {
                        showViewList();
                    }
                    break;
                case ListDetailsActivity.OPERATION_UPDATE:
                    currentShoppingLists.set(currentShoppingLists.indexOf(shoppingList), shoppingList);
                    adapter.notifyDataSetChanged();
                    break;
                case ListDetailsActivity.OPERATION_REMOVE:
                    currentShoppingLists.remove(shoppingList);
                    adapter.notifyDataSetChanged();
                    if (currentShoppingLists.size() == 0) {
                        showViewNoListsMessage();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Hides loading view and "No lists" message, and shows list.
     */
    private void showViewList() {
        listRV.setVisibility(View.VISIBLE);
        loadingMPB.setVisibility(View.GONE);
        noListsLL.setVisibility(View.GONE);
    }

    /**
     * Hides loading and shows list if there are any shopping lists in provided list,
     * otherwise shows "No lists" message.
     * @param objects list of shopping lists to show
     */
    private void showViewList(ArrayList<ShoppingList> objects) {
        currentShoppingLists = objects;
        if (objects == null || objects.size() == 0) {
            //if there are no shopping lists show message
            showViewNoListsMessage();
        } else {
            //else show list with objects
            listRV.setVisibility(View.VISIBLE);
            loadingMPB.setVisibility(View.GONE);
            noListsLL.setVisibility(View.GONE);

            adapter.setShoppingLists(objects);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows loading view and hides list and "No lists" message.
     */
    private void showViewLoading() {
        listRV.setVisibility(View.GONE);
        loadingMPB.setVisibility(View.VISIBLE);
        noListsLL.setVisibility(View.GONE);
    }

    /**
     * Shows "No lists" message and hides list and loading views.
     */
    private void showViewNoListsMessage() {
        listRV.setVisibility(View.GONE);
        loadingMPB.setVisibility(View.GONE);
        noListsLL.setVisibility(View.VISIBLE);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "restoreInstanceState()");
        currentShoppingLists = savedInstanceState.getParcelableArrayList(STATE_SHOPPING_LISTS);
        showViewList(currentShoppingLists);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState()");
        outState.putParcelableArrayList(STATE_SHOPPING_LISTS, currentShoppingLists);
        super.onSaveInstanceState(outState);
    }
}
