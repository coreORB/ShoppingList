package pl.coreorb.shoppinglist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import pl.coreorb.shoppinglist.BuildConfig;
import pl.coreorb.shoppinglist.R;
import pl.coreorb.shoppinglist.TestData;
import pl.coreorb.shoppinglist.contentprovideraccess.CreateCallback;
import pl.coreorb.shoppinglist.pojos.ShoppingList;
import pl.coreorb.shoppinglist.utils.Logger;

/**
 * Activity class showing active or archived shopping lists. New button is shown only when viewing
 * active shopping lists.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String STATE_SHOW_ARCHIVED = "state_show_archived";

    private MainActivityFragment mainFragment;
    private FloatingActionButton fab;

    private Animation scaleTo0Overshot;
    private Animation scaleTo100Overshot;

    private boolean showArchived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.activity_main_title);

        mainFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        prepareAnimations();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainFragment != null) {
                    mainFragment.startNewListActivity(fab);
                }
            }
        });
    }

    /**
     * Prepares animations for floating action button to be used.
     */
    private void prepareAnimations() {
        scaleTo0Overshot = AnimationUtils.loadAnimation(this, R.anim.scale_100_to_0_anticipate);
        scaleTo0Overshot.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });

        scaleTo100Overshot = AnimationUtils.loadAnimation(this, R.anim.scale_0_to_100_overshot);
        scaleTo100Overshot.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //do nothing
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //do nothing
            }
        });
    }

    /**
     * Changes activity title and floating action button visibility depending on parameter.
     * @param archived wherever activity shows archived shopping list or not
     */
    private void switchShoppingListsType(boolean archived, boolean animate) {
        Logger.v(LOG_TAG, "switchShoppingListsType()");
        showArchived = archived;
        if (archived) {
            if (animate) {
                fab.startAnimation(scaleTo0Overshot);
            } else {
                fab.setVisibility(View.GONE);
            }
            setTitle(R.string.activity_main_title_archived);
        } else {
            if (animate) {
                fab.startAnimation(scaleTo100Overshot);
            } else {
                fab.setVisibility(View.VISIBLE);
            }
            setTitle(R.string.activity_main_title);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showArchived) {
            //standard menu shown on archived shopping lists
            getMenuInflater().inflate(R.menu.menu_main_archived, menu);
        } else if (BuildConfig.DEBUG) {
            //special debug menu shown on active shopping lists
            getMenuInflater().inflate(R.menu.menu_main_debug, menu);
        } else {
            //standard menu shown on active shopping lists
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_archived) {
            switchShoppingListsType(true, true);
            mainFragment.setShownShoppingListsToArchived(true);
            return true;
        } else if (id == R.id.action_active) {
            switchShoppingListsType(false, true);
            mainFragment.setShownShoppingListsToArchived(false);
            return true;
        } else if (id == R.id.action_insert_test_data_en) {
            createTestData("en");
            return true;
        } else if (id == R.id.action_insert_test_data_pl) {
            createTestData("pl");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        switchShoppingListsType(savedInstanceState.getBoolean(STATE_SHOW_ARCHIVED), false);
        Logger.v(LOG_TAG, "onRestoreInstanceState() archived: " + showArchived);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.v(LOG_TAG, "onSaveInstanceState() archived: " + showArchived);
        outState.putBoolean(STATE_SHOW_ARCHIVED, showArchived);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mainFragment != null) {
            mainFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Method for debugging purposes inserts new test data into database.
     * @param lang language, currently: "en" or "pl"
     */
    private void createTestData(String lang) {
        CreateCallback<ShoppingList> callback = new CreateCallback<ShoppingList>() {
            @Override
            public void onSuccess(ShoppingList object) {
                //refresh data
                mainFragment.setShownShoppingListsToArchived(false);
            }

            @Override
            public void onFailure() {
                //not used here
            }
        };
        (new TestData()).createTestData(getContentResolver(), lang, callback);
    }
}
