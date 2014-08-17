package gr.tsagi.jekyllforandroid.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.adapters.NavDrawerListAdapter;
import gr.tsagi.jekyllforandroid.fragments.PostsListFragment;
import gr.tsagi.jekyllforandroid.fragments.PrefsFragment;
import gr.tsagi.jekyllforandroid.utils.FetchPostsTask;
import gr.tsagi.jekyllforandroid.utils.NavDrawerItem;

/**
 * Created by tsagi on 9/9/13.
 */

public class PostsListActivity extends FragmentActivity {

    private static final String LOG_TAG = PostsListActivity.class.getSimpleName();

    public static final String POST_STATUS = "post_status";

    String mUsername;
    String mToken;
    String mRepo;
    SharedPreferences settings;

    FetchPostsTask fetchPostsTask;

    private String[] mNavTitles;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);

        restorePreferences();
        DrawerSetup();
        updateList();

        if (mToken.equals("")) {
            login();
        }

        if (mRepo.isEmpty() && !mToken.equals("")) {
            Toast.makeText(PostsListActivity.this,
                    "There is something wrong with your jekyll repo",
                    Toast.LENGTH_LONG).show();
            login();
        } else {
            // Select default screen.
            selectItem(0);
        }

    }

    private void updateList() {
        fetchPostsTask = new FetchPostsTask(this);
        fetchPostsTask.execute();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        // if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Just for the logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutDialog();
                return true;
            case R.id.action_new:
                newPost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open,
        // hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_new).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void DrawerSetup() {
        mNavTitles = getResources().getStringArray(R.array.nav_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerTitle = getResources().getString(R.string.app_name);

        final TypedArray navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons_dark);
        ArrayList<NavDrawerItem> navDrawerItems;
        NavDrawerListAdapter adapter;

        navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(mNavTitles[0],
                navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavTitles[1],
                navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(mNavTitles[2],
                navMenuIcons.getResourceId(2, -1)));

        navMenuIcons.recycle();

        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);


        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state.*/
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // just styling option add shadow the right edge of the drawer
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view,
                                int position, long id) {
            Log.d(LOG_TAG, "clicked position");
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on
        // position

        Fragment fragment = null;
        Bundle data = new Bundle();
        data.putInt(PostsListActivity.POST_STATUS, position);

        switch (position) {
            case 0:
                try {
                    fragment = new PostsListFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    fragment = new PostsListFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new PrefsFragment()).commit();
                break;
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        if (position != 2) {
            fragment.setArguments(data);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Start new post or continue working on your draft
     */
    public void newPost() {
        Intent myIntent = new Intent(PostsListActivity.this,
                EditPostActivity.class);
        startActivity(myIntent);
    }

    /**
     * Logout and clear settings
     */
    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Shared preferences and Intent settings
        // before logout ask user and remind him any draft posts

        final SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        if (sharedPreferences.getString("draft_content", "").equals(""))
            builder.setMessage(R.string.dialog_logout_nodraft);
        else
            builder.setMessage(R.string.dialog_logout_draft);

        // Add the buttons
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        // Clear credentials and Drafts
                        login();
                    }
                }
        );
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }
        );

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();

    }

    private void restorePreferences() {
        settings = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        mUsername = settings.getString("user_login", "");
        mToken = settings.getString("user_status", "");
        mRepo = settings.getString("user_repo", "");

    }

    private void login() {
        Intent myIntent = new Intent(PostsListActivity.this,
                LoginActivity.class);
        SharedPreferences sharedPreferences = getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.commit();

        startActivity(myIntent);
    }
}

