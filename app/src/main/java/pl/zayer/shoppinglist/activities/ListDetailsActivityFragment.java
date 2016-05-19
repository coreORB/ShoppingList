package pl.zayer.shoppinglist.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.contentprovideraccess.ContentProviderAccess;
import pl.zayer.shoppinglist.contentprovideraccess.CreateCallback;
import pl.zayer.shoppinglist.contentprovideraccess.DeleteCallback;
import pl.zayer.shoppinglist.contentprovideraccess.SelectCallback;
import pl.zayer.shoppinglist.contentprovideraccess.UpdateCallback;
import pl.zayer.shoppinglist.pojos.Item;
import pl.zayer.shoppinglist.pojos.ShoppingList;
import pl.zayer.shoppinglist.utils.AnimationFinishedCallback;

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

    private Animation scale90To100FadeIn;
    private Animation scale100To90FadeOut;
    //booleans used in method hideAllViews()
    boolean listHidden, loadingHidden = false;

    private ContentProviderAccess contentProviderAccess;
    private ShoppingList currentShoppingList;

    private boolean queryRunning = false;

    public static ListDetailsActivityFragment newInstance() {
        return new ListDetailsActivityFragment();
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
        Log.v(LOG_TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_list_details, container, false);

        //get views
        listRV = (RecyclerView) rootView.findViewById(R.id.list_rv);
        loadingMPB = (MaterialProgressBar) rootView.findViewById(R.id.loading_mpb);

        //get animations
        scale90To100FadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_90_to_100_fade_in);
        scale100To90FadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.scale_100_to_90_fade_out);

        contentProviderAccess = new ContentProviderAccess(getActivity().getContentResolver());

        hideAllViews(false, null);

        Bundle args = getArguments();
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else if (args != null && args.containsKey(ARG_SHOPPING_LIST)) {
            showViewLoading(false, null);
            onCreateViewEditList((ShoppingList) args.getParcelable(ARG_SHOPPING_LIST));
        } else {
            showViewLoading(false, null);
            onCreateViewNewList();
        }

        return rootView;
    }

    /**
     * Continuations of onCreate() method in case of creating new list.
     */
    private void onCreateViewNewList() {
        Log.v(LOG_TAG, "onCreateViewNewList()");
        currentShoppingList = new ShoppingList();

        //create new shopping list in database
        CreateCallback<ShoppingList> callback = new CreateCallback<ShoppingList>() {
            @Override
            public void onSuccess(final ShoppingList object) {
                object.setFullyLoaded(true);
                hideViewLoading(true, new AnimationFinishedCallback() {
                    @Override
                    public void animationFinished() {
                        setUpAndShowList(object.isArchived(), object.getTitle(), object.getItems(), true);
                    }
                });
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

    /**
     * Continuations of onCreate() method in case of editing existing list.
     *
     * @param initialShoppingList shopping list to edit
     */
    private void onCreateViewEditList(final ShoppingList initialShoppingList) {
        Log.v(LOG_TAG, "onCreateViewEditList(" + initialShoppingList + ")");
        currentShoppingList = initialShoppingList;

        //if object if already fully loaded just read data from object
        if (initialShoppingList.isFullyLoaded()) {
            hideViewLoading(true, new AnimationFinishedCallback() {
                @Override
                public void animationFinished() {
                    setUpAndShowList(initialShoppingList.isArchived(),
                            initialShoppingList.getTitle(),
                            currentShoppingList.getItems(), true);
                }
            });
        } else {
            //get items for current shopping list
            SelectCallback<Item> callback = new SelectCallback<Item>() {
                @Override
                public void onSuccess(ArrayList<Item> objects) {
                    currentShoppingList.setItems(objects);
                    currentShoppingList.setFullyLoaded(true);
                    hideViewLoading(true, new AnimationFinishedCallback() {
                        @Override
                        public void animationFinished() {
                            setUpAndShowList(currentShoppingList.isArchived(),
                                    currentShoppingList.getTitle(),
                                    currentShoppingList.getItems(), true);
                        }
                    });

                }

                @Override
                public void onFailure(int errorCode) {
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

    private void setUpAndShowList(boolean isArchived, String title, ArrayList<Item> items, boolean animate) {
        Log.v(LOG_TAG, "setUpAndShowList()");
        adapter = new ListDetailsActivityFragmentListAdapter(ListDetailsActivityFragment.this.getContext(),
                isArchived,
                title,
                items,
                this, this, this, this);
        listRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listRV.setAdapter(adapter);
        showViewList(animate, null);
    }

    @Override
    public void onItemAdded(final Item item) {
        Log.v(LOG_TAG, "onItemAdded(" + item + ")");
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
        Log.v(LOG_TAG, "onItemEdited(" + item + ")");
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
        Log.v(LOG_TAG, "onItemRemoveClicked(" + item + ")");
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
        Log.v(LOG_TAG, "onShoppingListTitleEdited(" + title + ")");
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

    /**
     * Hides all views (list and loading).
     *
     * @param animate  if true hiding will be animated, otherwise views will hide instantly
     * @param callback called when all views are done hiding (optional)
     */
    private void hideAllViews(boolean animate, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideAllViews(" + animate + ")");

        hideViewList(animate, new AnimationFinishedCallback() {
            @Override
            public void animationFinished() {
                listHidden = true;
                if (listHidden && loadingHidden) {
                    if (callback != null) callback.animationFinished();
                }
            }
        });
        hideViewLoading(animate, new AnimationFinishedCallback() {
            @Override
            public void animationFinished() {
                loadingHidden = true;
                if (listHidden && loadingHidden) {
                    if (callback != null) callback.animationFinished();
                }
            }
        });
    }

    /**
     * Shows view with list of items in shopping lists and title.
     *
     * @param animate  if true showing view will be animated, otherwise change will be instant
     * @param callback called when view is done showing (optional)
     */
    private void showViewList(boolean animate, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "showViewList(" + animate + ", " + callback + ")");
        if (listRV.getVisibility() == View.VISIBLE) {
            //view is already shown, just exit
            Log.d(LOG_TAG, "view already shown");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        if (animate) {
            scale90To100FadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    listRV.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            listRV.startAnimation(scale90To100FadeIn);
        } else {
            listRV.setVisibility(View.VISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Hides view with list of items in shopping lists and title.
     *
     * @param animate  if true hiding view will be animated, otherwise change will be instant
     * @param callback called when view is done hiding (optional)
     */
    public void hideViewList(boolean animate, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideViewList(" + animate + ", " + callback + ")");
        if (listRV.getVisibility() == View.GONE || listRV.getVisibility() == View.INVISIBLE) {
            //view is already hidden, just exit
            Log.d(LOG_TAG, "view already hidden");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        if (animate) {
            scale100To90FadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listRV.setVisibility(View.INVISIBLE);
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            listRV.startAnimation(scale100To90FadeOut);
        } else {
            listRV.setVisibility(View.INVISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Shows view with loading indicator.
     *
     * @param animate  if true showing view will be animated, otherwise change will be instant
     * @param callback called when view is done showing (optional)
     */
    public void showViewLoading(boolean animate, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "showViewLoading(" + animate + ", " + callback + ")");
        if (loadingMPB.getVisibility() == View.VISIBLE) {
            //view is already shown, just exit
            Log.d(LOG_TAG, "view already shown");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        if (animate) {
            scale90To100FadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    loadingMPB.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            loadingMPB.startAnimation(scale90To100FadeIn);
        } else {
            loadingMPB.setVisibility(View.VISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Hides view with loading indicator.
     *
     * @param animate  if true hiding view will be animated, otherwise change will be instant
     * @param callback called when view is done hiding (optional)
     */
    private void hideViewLoading(boolean animate, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideViewLoading(" + animate + ", " + callback + ")");
        if (loadingMPB.getVisibility() == View.GONE || loadingMPB.getVisibility() == View.INVISIBLE) {
            //view is already hidden, just exit
            Log.d(LOG_TAG, "view already hidden");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        if (animate) {
            scale100To90FadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    loadingMPB.setVisibility(View.INVISIBLE);
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            loadingMPB.startAnimation(scale100To90FadeOut);
        } else {
            loadingMPB.setVisibility(View.INVISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    public ShoppingList getCurrentShoppingList() {
        return currentShoppingList;
    }

    public boolean isQueryRunning() {
        return queryRunning;
    }

    /**
     * Restores state of this fragment.
     *
     * @param savedInstanceState bundle with saved state
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "restoreInstanceState()");
        currentShoppingList = savedInstanceState.getParcelable(STATE_CURRENT_SHOPPING_LIST);
        setUpAndShowList(currentShoppingList.isArchived(),
                currentShoppingList.getTitle(),
                currentShoppingList.getItems(), false);
    }

    /**
     * Saves state of this fragment info a bundle.
     * @param outState bundle to which state will be saved
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState()");
        outState.putParcelable(STATE_CURRENT_SHOPPING_LIST, currentShoppingList);
        super.onSaveInstanceState(outState);
    }
}
