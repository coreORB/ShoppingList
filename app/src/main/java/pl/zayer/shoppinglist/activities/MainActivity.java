package pl.zayer.shoppinglist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import pl.zayer.shoppinglist.R;

/**
 * Activity class showing active or archived shopping lists. New button is shown only when viewing
 * active shopping lists.
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String STATE_SHOW_ARCHIVED = "state_show_archived";

    private MainActivityFragment mainFragment;
    private FloatingActionButton fab;

    private boolean showArchived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainFragment != null) {
                    mainFragment.startNewListActivity();
                }
            }
        });
    }

    /**
     * Changes activity title and floating action button visibility depending on parameter.
     * @param archived wherever activity shows arhived shopping list or not
     */
    private void switchShoppingListsType(boolean archived) {
        Log.i(LOG_TAG, "switchShoppingListsType()");
        showArchived = archived;
        if (archived) {
            fab.setVisibility(View.GONE);
            setTitle(R.string.activity_main_title_archived);
        } else {
            fab.setVisibility(View.VISIBLE);
            setTitle(R.string.activity_main_title);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showArchived) {
            getMenuInflater().inflate(R.menu.menu_main_archived, menu);
        } else {
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
            switchShoppingListsType(true);
            mainFragment.setShownShoppingListsToArchived(true);
            return true;
        } else if (id == R.id.action_active) {
            switchShoppingListsType(false);
            mainFragment.setShownShoppingListsToArchived(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        switchShoppingListsType(savedInstanceState.getBoolean(STATE_SHOW_ARCHIVED));
        Log.i(LOG_TAG, "onRestoreInstanceState() archived: " + showArchived);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState() archived: " + showArchived);
        outState.putBoolean(STATE_SHOW_ARCHIVED, showArchived);
        super.onSaveInstanceState(outState);
    }
}
