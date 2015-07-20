package com.bbingju.mymemo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.util.List;

public class ListMemoActivity extends Activity {

    private static final int LOGIN_REQUEST_CODE = 100;
    private static final int EDIT_REQUEST_CODE = 200;

    private LayoutInflater inflater;
    private ParseQueryAdapter<Memo> memoListAdapter;

    private ListView memoListView;
    private LinearLayout noMemosView;
    private TextView loggedInInfoView;

    private ParseUser currentUser;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);

        noMemosView = (LinearLayout) findViewById(R.id.no_memos_view);
        memoListView = (ListView) findViewById(R.id.memo_list_view);
        memoListView.setEmptyView(noMemosView);
        loggedInInfoView = (TextView) findViewById(R.id.loggedin_info);

        ParseQueryAdapter.QueryFactory<Memo> factory = new ParseQueryAdapter.QueryFactory<Memo>() {
            public ParseQuery<Memo> create() {
                ParseQuery<Memo> query = Memo.getQuery();
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();
                return query;
            }
        };

        // Set up the adapter
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        memoListAdapter = new MemoListAdapter(this, factory);

        ListView memoListView = (ListView) findViewById(R.id.memo_list_view);
        memoListView.setAdapter(memoListAdapter);

        memoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Memo memo = memoListAdapter.getItem(position);
                openEditView(memo);
            }
        });

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = ParseUser.getCurrentUser();
    }

    @Override
    protected void onResume() {

        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        currentUser = ParseUser.getCurrentUser();

        if (!ParseAnonymousUtils.isLinked(currentUser)) {
            syncMemosToParse();
            updateLoggedInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void updateLoggedInfo() {
        if (!ParseAnonymousUtils.isLinked(currentUser)) {
            loggedInInfoView.setText(getString(R.string.logged_in, currentUser.getString("name")));
        } else {
            loggedInInfoView.setText(getString(R.string.not_logged_in));
        }
    }

    private void openEditView(Memo memo) {
        Intent i = new Intent(this, EditMemoActivity.class);
        i.putExtra("ID", memo.getUuidString());
        startActivityForResult(i, EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_REQUEST_CODE) {
                memoListAdapter.loadObjects();
            } else if (requestCode == LOGIN_REQUEST_CODE) {
                currentUser = ParseUser.getCurrentUser();
                if (currentUser.isNew()) {
                    syncMemosToParse();
                } else {
                    loadFromParse();
                }
            }
        }
    }

    private void loadFromParse() {
        ParseQuery<Memo> query = Memo.getQuery();
        query.whereEqualTo("author", currentUser);
        query.findInBackground(new FindCallback<Memo>() {
                                   @Override
                                   public void done(List<Memo> list, ParseException e) {
                                       if (e == null) {
                                           ParseObject.pinAllInBackground(list,
                                                   new SaveCallback() {
                                                       @Override
                                                       public void done(ParseException e) {
                                                           if (e == null) {
                                                               if (!isFinishing()) {
                                                                   memoListAdapter.loadObjects();
                                                               } else {
                                                                   Log.i("ListMemoActivity",
                                                                           "Error pinning memos: " +
                                                                                   e.getMessage());
                                                               }
                                                           }
                                                       }
                                                   });
                                       }
                                   }
                               }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_new) {
            if (currentUser != null) {
                startActivityForResult(new Intent(this, EditMemoActivity.class),
                        EDIT_REQUEST_CODE);
            }
        }

        if (id == R.id.action_sync) {
            syncMemosToParse();
        }

        if (id == R.id.action_login) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_REQUEST_CODE);
        }

        if (id == R.id.action_logout) {
            ParseUser.logOut();
            ParseAnonymousUtils.logIn(null);
            currentUser = ParseUser.getCurrentUser();

            updateLoggedInfo();
            memoListAdapter.clear();
            ParseObject.unpinAllInBackground(MemoApplication.MEMO_GROUP_NAME);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser());
        menu.findItem(R.id.action_login).setVisible(!realUser);
        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }

    private void syncMemosToParse() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (!ParseAnonymousUtils.isLinked(currentUser)) {
                ParseQuery<Memo> query = Memo.getQuery();
                query.fromPin(MemoApplication.MEMO_GROUP_NAME);
                query.whereEqualTo("isDraft", true);
                query.findInBackground(new FindCallback<Memo>() {
                        @Override
                            public void done(List<Memo> list, ParseException e) {
                            if (e == null) {
                                for (final Memo memo : list) {
                                    memo.setDraft(false);
                                    memo.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    if (!isFinishing()) {
                                                        memoListAdapter.notifyDataSetChanged();
                                                    }
                                                } else {
                                                    memo.setDraft(true);
                                                }
                                            }
                                        });
                                }
                            } else {
                                Log.i("ListMemoActivity",
                                      "syncMemosToParse: Error finding pinned memos: "
                                      + e.getMessage());
                            }
                        }
                    });
            } else {
                // If we have a network connection but no logged in user, direct
                // the person to log in or sign up.
                ParseLoginBuilder builder = new ParseLoginBuilder(this);
                startActivityForResult(builder.build(), LOGIN_REQUEST_CODE);
            }
        } else {
            // If there is no connection, let the user know the sync didn't
            // happen
            Toast.makeText(
                           getApplicationContext(),
                           "Your device appears to be offline. Some memos may not have been synced to Parse.",
                           Toast.LENGTH_LONG).show();
        }
    }

    private static class ViewHolder {
        TextView memoText;
    }

    private class MemoListAdapter extends ParseQueryAdapter<Memo> {

        public MemoListAdapter(Context context, ParseQueryAdapter.QueryFactory<Memo> queryFactory) {
            super(context, queryFactory);
        }

        @Override
        public View getItemView(Memo memo, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_memo, parent, false);
                holder = new ViewHolder();
                holder.memoText = (TextView) view.findViewById(R.id.memo_content);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            TextView content = holder.memoText;
            content.setText(memo.getMemo());
            return view;
        }
    }
}
