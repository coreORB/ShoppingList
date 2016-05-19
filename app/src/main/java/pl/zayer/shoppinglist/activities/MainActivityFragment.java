package pl.zayer.shoppinglist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.util.ArrayList;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import pl.zayer.shoppinglist.R;
import pl.zayer.shoppinglist.contentprovideraccess.ContentProviderAccess;
import pl.zayer.shoppinglist.contentprovideraccess.SelectCallback;
import pl.zayer.shoppinglist.pojos.ShoppingList;
import pl.zayer.shoppinglist.utils.AnimationFinishedCallback;

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

    private Animation fadeIn;
    private Animation fadeOut;
    private Animation fadeInMoveUp;
    private Animation fadeOutMoveDown;
    private Animation scale110To100FadeIn;
    private Animation scale100To110FadeOut;
    //booleans used in method hideAllViews()
    boolean listHidden, loadingHidden, noListsHidden = false;

    private ArrayList<ShoppingList> currentShoppingLists;

    private boolean showingArchivedLists = false;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //get views
        listRV = (RecyclerView) rootView.findViewById(R.id.list_rv);
        loadingMPB = (MaterialProgressBar) rootView.findViewById(R.id.loading_mpb);
        noListsLL = (LinearLayout) rootView.findViewById(R.id.no_lists_ll);

        //get animations
        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        fadeInMoveUp = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_move_up);
        fadeOutMoveDown = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_move_down);
        scale110To100FadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_110_to_100_fade_in);
        scale100To110FadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.scale_100_to_110_fade_out);

        //set up list
        adapter = new MainActivityFragmentListAdapter(getContext(), this);
        listRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listRV.setAdapter(adapter);

        hideAllViews(false, null, null);

        if (savedInstanceState == null) {
            showViewLoading(false, null);
            loadShoppingLists(false, null);
        } else {
            restoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    /**
     * Shows loading view and loads from database list of shopping lists (archived or active).
     * @param archived wherever to show archived shopping list or not
     */
    public void setShownShoppingListsToArchived(final boolean archived) {
        Log.v(LOG_TAG, "setShownShoppingListsToArchived()");
        showingArchivedLists = archived;
        hideAllViews(true, archived ? scale100To110FadeOut : fadeOutMoveDown,
                new AnimationFinishedCallback() {
                    @Override
                    public void animationFinished() {
                        showViewLoading(true, null);
                        loadShoppingLists(archived, archived ? fadeInMoveUp : scale110To100FadeIn);
                    }
                }
        );
    }

    /**
     * Loads from database shopping lists and displays them in RecyclerView.
     * @param archived wherever to load archived shopping list or not
     * @param anim animation used to show view with result (optional)
     */
    private void loadShoppingLists(final boolean archived, @Nullable final Animation anim) {
        Log.v(LOG_TAG, "loadShoppingLists(" + archived + ")");
        ContentProviderAccess cpa = new ContentProviderAccess(getActivity().getContentResolver());
        SelectCallback<ShoppingList> callback = new SelectCallback<ShoppingList>() {
            @Override
            public void onSuccess(final ArrayList<ShoppingList> objects) {
                hideViewLoading(true, new AnimationFinishedCallback() {
                    @Override
                    public void animationFinished() {
                        showData(true, anim, objects);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode) {
                hideViewLoading(true, new AnimationFinishedCallback() {
                    @Override
                    public void animationFinished() {
                        showViewNoListsMessage(true, anim, null);
                    }
                });
                Snackbar.make(getView(), R.string.fragment_main_error_message_reading_shopping_lists, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadShoppingLists(archived, anim);
                            }
                        }).show();
            }
        };
        cpa.getShoppingListsAndUncheckedItems(archived, callback);
    }

    /**
     * Starts ListDetailsActivity to create new shopping list.
     * @param view clicked view
     */
    public void startNewListActivity(View view) {
        Log.v(LOG_TAG, "startNewListActivity()");
        Intent intent = new Intent(getContext(), ListDetailsActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                view, (int) view.getX(), (int) view.getY(), view.getWidth(), view.getHeight());
        ActivityCompat.startActivityForResult(MainActivityFragment.this.getActivity(), intent,
                REQUEST_CODE_LIST_DETAILS, options.toBundle());
    }

    /**
     * Starts ListDetailsActivity to edit selected shopping list.
     * @param shoppingList shopping list selected
     * @param view clicked view
     */
    @Override
    public void onShoppingListClicked(ShoppingList shoppingList, View view) {
        Log.v(LOG_TAG, "onShoppingListClicked(" + shoppingList + ")");
        Intent intent = new Intent(MainActivityFragment.this.getContext(), ListDetailsActivity.class);
        intent.putExtra(ListDetailsActivity.ARG_SHOPPING_LIST, shoppingList);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                view, (int) view.getX(), (int) view.getY(), view.getWidth(), view.getHeight());
        ActivityCompat.startActivityForResult(MainActivityFragment.this.getActivity(), intent,
                REQUEST_CODE_LIST_DETAILS, options.toBundle());
    }

    /**
     * Handles result of ListDetailsActivity and updates shown shopping lists RecyclerView to
     * reflect changes made during creating/editing shopping list.
     * @param requestCode request code
     * @param resultCode result code
     * @param data returned data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "onActivityResult()");
        if (requestCode == REQUEST_CODE_LIST_DETAILS && resultCode == Activity.RESULT_OK) {
            ShoppingList shoppingList = data.getParcelableExtra(ListDetailsActivity.RESULT_SHOPPING_LIST);
            int operation = data.getIntExtra(ListDetailsActivity.RESULT_OPERATION, -1);
            switch (operation) {
                case ListDetailsActivity.OPERATION_ADD:
                    currentShoppingLists.add(0, shoppingList);
                    adapter.notifyDataSetChanged();
                    if (adapter.getItemCount() == 1) {
                        hideViewNoListsMessage(true, showingArchivedLists ? fadeOutMoveDown : scale100To110FadeOut,
                                new AnimationFinishedCallback() {
                                    @Override
                                    public void animationFinished() {
                                        showViewList(true, showingArchivedLists ? fadeInMoveUp : scale110To100FadeIn, null);
                                    }
                                });
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
                        hideViewList(true, showingArchivedLists ? fadeOutMoveDown : scale100To110FadeOut,
                                new AnimationFinishedCallback() {
                                    @Override
                                    public void animationFinished() {
                                        showViewNoListsMessage(true, showingArchivedLists ? fadeInMoveUp : scale110To100FadeIn, null);
                                    }
                                });
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Shows list if there are any shopping lists in provided list,
     * otherwise shows "No lists" message.
     * @param animate if true showing view will be animated, otherwise change will be instant
     * @param anim Animation object used to show view with result (optional)
     * @param objects list of shopping lists to show
     */
    private void showData(final boolean animate, Animation anim, final ArrayList<ShoppingList> objects) {
        Log.v(LOG_TAG, "showData(" + animate + ", " + objects + ")");
        currentShoppingLists = objects;

        if (objects == null || objects.size() == 0) {
            //if there are no shopping lists show message
            showViewNoListsMessage(animate, anim, null);
        } else {
            adapter.setShoppingLists(objects);
            adapter.notifyDataSetChanged();
            showViewList(animate, anim, null);
        }
    }

    /**
     * Hides all views (list, loading and "No lists" message).
     * @param animate if true hiding will be animated, otherwise views will hide instantly
     * @param anim Animation to use when hiding views: list and "No lists" message (optional)
     * @param callback called when all views are done hiding (optional)
     */
    private void hideAllViews(boolean animate, @Nullable Animation anim, @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideAllViews(" + animate + ")");

        hideViewList(animate, anim, new AnimationFinishedCallback() {
            @Override
            public void animationFinished() {
                listHidden = true;
                if (listHidden && loadingHidden && noListsHidden) {
                    if (callback != null) callback.animationFinished();
                }
            }
        });
        hideViewLoading(animate, new AnimationFinishedCallback() {
            @Override
            public void animationFinished() {
                loadingHidden = true;
                if (listHidden && loadingHidden && noListsHidden) {
                    if (callback != null) callback.animationFinished();
                }
            }
        });
        hideViewNoListsMessage(animate, anim, new AnimationFinishedCallback() {
            @Override
            public void animationFinished() {
                noListsHidden = true;
                if (listHidden && loadingHidden && noListsHidden) {
                    if (callback != null) callback.animationFinished();
                }
            }
        });
    }

    /**
     * Shows view with list of shopping lists. Should be called only in showData() method.
     *
     * @param animate  if true showing view will be animated, otherwise change will be instant
     * @param anim     Animation object to use for animation (optional)
     * @param callback called when view is done showing (optional)
     */
    private void showViewList(boolean animate, @Nullable Animation anim,
                              @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "showViewList(" + animate + ", " + callback + ")");
        if (listRV.getVisibility() == View.VISIBLE) {
            //view is already shown, just exit
            Log.d(LOG_TAG, "view already shown");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        //set animation
        Animation animation;
        if (anim != null) {
            animation = anim;
        } else {
            animation = scale110To100FadeIn;
        }

        if (animate) {
            animation.setAnimationListener(new Animation.AnimationListener() {
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
            listRV.startAnimation(animation);
        } else {
            listRV.setVisibility(View.VISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Hides view with list of shopping lists.
     *
     * @param animate  if true hiding view will be animated, otherwise change will be instant
     * @param anim     Animation object to use for animation (optional)
     * @param callback called when view is done hiding (optional)
     */
    private void hideViewList(boolean animate, @Nullable Animation anim,
                              @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideViewList(" + animate + ", " + callback + ")");
        if (listRV.getVisibility() == View.GONE || listRV.getVisibility() == View.INVISIBLE) {
            //view is already hidden, just exit
            Log.d(LOG_TAG, "view already hidden");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        //set animation
        Animation animation;
        if (anim != null) {
            animation = anim;
        } else {
            animation = scale100To110FadeOut;
        }

        if (animate) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listRV.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            listRV.startAnimation(animation);
        } else {
            listRV.setVisibility(View.GONE);
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
    private void showViewLoading(boolean animate, @Nullable final AnimationFinishedCallback callback) {
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
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
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
            loadingMPB.startAnimation(fadeIn);
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
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    loadingMPB.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            loadingMPB.startAnimation(fadeOut);
        } else {
            loadingMPB.setVisibility(View.GONE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Shows view with "no lists" message. Should be called only from showData() method.
     *
     * @param animate  if true showing view will be animated, otherwise change will be instant
     * @param anim     Animation object to use for animation (optional)
     * @param callback called when view is done showing (optional)
     */
    private void showViewNoListsMessage(boolean animate, @Nullable Animation anim,
                                        @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "showViewNoListsMessage(" + animate + ", " + callback + ")");
        if (noListsLL.getVisibility() == View.VISIBLE) {
            //view is already shown, just exit
            Log.d(LOG_TAG, "view already shown");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        //set animation
        Animation animation;
        if (anim != null) {
            animation = anim;
        } else {
            animation = scale110To100FadeIn;
        }

        if (animate) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    noListsLL.setVisibility(View.VISIBLE);
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
            noListsLL.startAnimation(animation);
        } else {
            noListsLL.setVisibility(View.VISIBLE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Hides view with "no lists" message.
     *
     * @param animate  if true hiding view will be animated, otherwise change will be instant
     * @param anim     Animation object to use for animation (optional)
     * @param callback called when view is done hiding (optional)
     */
    private void hideViewNoListsMessage(boolean animate, @Nullable Animation anim,
                                        @Nullable final AnimationFinishedCallback callback) {
        Log.v(LOG_TAG, "hideViewNoListsMessage(" + animate + ", " + callback + ")");
        if (noListsLL.getVisibility() == View.GONE || noListsLL.getVisibility() == View.INVISIBLE) {
            //view is already hidden, just exit
            Log.d(LOG_TAG, "view already hidden");
            if (callback != null) {
                callback.animationFinished();
            }
            return;
        }

        //set animation
        Animation animation;
        if (anim != null) {
            animation = anim;
        } else {
            animation = scale100To110FadeOut;
        }

        if (animate) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    noListsLL.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.animationFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //do nothing
                }
            });
            noListsLL.startAnimation(animation);
        } else {
            noListsLL.setVisibility(View.GONE);
            if (callback != null) {
                callback.animationFinished();
            }
        }
    }

    /**
     * Restores state of this fragment.
     *
     * @param savedInstanceState bundle with saved state
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "restoreInstanceState()");
        currentShoppingLists = savedInstanceState.getParcelableArrayList(STATE_SHOPPING_LISTS);
        showData(false, null, currentShoppingLists);
    }

    /**
     * Saves state of this fragment info a bundle.
     * @param outState bundle to which state will be saved
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState()");
        outState.putParcelableArrayList(STATE_SHOPPING_LISTS, currentShoppingLists);
        super.onSaveInstanceState(outState);
    }
}
