package com.example.android.readme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<News>> {
    private static final java.lang.String CONTACT_FRAGMENT = "contact_fragment";
    private static final java.lang.String ABOUT_FRAGMENT = "about_fragment";
    private static final int LOADER_ID = 1;
    private NewsAdapter adapter;
    private ConnectivityManager connectivityManager;
    private int chosenSection;
    private String TEST_URL = "http://content.guardianapis.com/search?order-by=newest&api-key=test&show-fields=thumbnail,body&show-tags=contributor";
    private ProgressBar loading;
    private RelativeLayout emptyView;
    private ImageView cloud;
    private TextView noInternet;
    private TextView noItem;
    private LoaderManager loaderManager;
    private ListView list;
    private ImageButton search;
    private EditText searchText;
    private String url;
    private String pageSize;
    public SharedPreferences sharedPreferences;
    private SwipeRefreshLayout swipe;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chosenSection = 0;
        //get performance view
        loading = findViewById(R.id.loading);
        emptyView = findViewById(R.id.empty_view);
        cloud = findViewById(R.id.image);
        noInternet = findViewById(R.id.no_internet);
        noItem = findViewById(R.id.text_no_item);
        //news adapter
        adapter = new NewsAdapter(this, new ArrayList<News>());
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //share preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        pageSize = sharedPreferences.getString(getString(R.string.setting_number_of_result), "");
        url = TEST_URL + "&page-size=" + pageSize;

        //load data
        loaderManager = getLoaderManager();
        callInBackGround();

        //search button
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        search = findViewById(R.id.update);
        searchText = findViewById(R.id.search);
        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == keyEvent.KEYCODE_ENTER) {
                    searchAction();
                    return true;
                }
                return false;
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchAction();
            }
        });
        //swipe to refresh
        swipe = findViewById(R.id.swipe_to_refresh);
        swipe.setRefreshing(false);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                callInBackGround();
            }
        });
    }


    private void searchAction() {
        hideKeyboard();
        if (!searchText.getText().toString().isEmpty()) {
            String searchWords = searchText.getText().toString().trim().replace("\\s", "%20");
            url = TEST_URL + "&q=" + searchWords;
            callInBackGround();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.enter_something), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void createNewUrl(int chosenSection) {
        switch (chosenSection) {
            case 1:
                url = TEST_URL + "&section=business";
                break;
            case 2:
                url = TEST_URL + "&section=technology";
                break;
            case 3:
                url = TEST_URL + "&section=lifeandstyle";
                break;
            case 4:
                url = TEST_URL + "&section=travel";
                break;
            case 5:
                url = TEST_URL + "&section=sport";
                break;
            case 6:
                url = TEST_URL + "&section=football";
                break;
            case 7:
                url = TEST_URL + "&section=film";
                break;
            case 8:
                url = TEST_URL + "&section=education";
                break;
            case 9:
                url = TEST_URL + "&section=environment";
                break;
            case 10:
                url = TEST_URL + "&section=culture";
                break;
            case 11:
                url = TEST_URL + "&section=fashion";
                break;
            default:
                break;
        }
    }

    private void callInBackGround() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo infor = connectivityManager.getActiveNetworkInfo();
        if (infor != null && infor.isConnected()) {
            setOnInternet();
            if (loaderManager.getLoader(LOADER_ID) == null) {
                Log.e("ini", "");
                loaderManager.initLoader(LOADER_ID, null, MainActivity.this);
            } else {
                Log.e("restart", "");
                loaderManager.restartLoader(LOADER_ID, null, MainActivity.this);
            }
            list.setSelection(0);

        } else {
            Log.e("no internet", "");
            adapter.clear();
            setOffInternet();
        }
    }

    private void setOnInternet() {
        emptyView.setVisibility(View.GONE);
    }

    private void setOffInternet() {
        loading.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        noInternet.setVisibility(View.VISIBLE);
        cloud.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items, menu);
        //spinner
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_list,
                android.R.layout.simple_dropdown_item_1line);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchText.setText("");
                chosenSection = i;
                createNewUrl(chosenSection);
                //Toast.makeText(MainActivity.this,chosenSection,Toast.LENGTH_SHORT).show();
                callInBackGround();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View setting = findViewById(R.id.setting_button);
        View saved = findViewById(R.id.saved_item);
        switch (item.getItemId()) {
            case R.id.setting_button:
                //set up pop up menu
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, setting);
                popupMenu.getMenuInflater().inflate(R.menu.popup_item, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.setting:
                                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.about:
                                DialogFragment aboutDialog = new AboutDialogFragment();
                                aboutDialog.show(getSupportFragmentManager(), CONTACT_FRAGMENT);
                                break;
                            case R.id.refresh:
                                callInBackGround();
                                break;
                            default:
                                DialogFragment feedbackDialog = new ContactDialogFragment();
                                feedbackDialog.show(getSupportFragmentManager(), ABOUT_FRAGMENT);

                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            case R.id.save:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<ArrayList<News>> onCreateLoader(int i, Bundle bundle) {
        adapter.clear();
        loading.setVisibility(View.VISIBLE);
        pageSize = sharedPreferences.getString(getString(R.string.setting_number_of_result), "");
        return new NewsLoader(MainActivity.this, url + "&page-size=" + pageSize);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<News>> loader, ArrayList<News> news) {
        loading.setVisibility(View.GONE);
        swipe.setRefreshing(false);
        if (news == null || news.isEmpty()) {
            noItem.setVisibility(View.VISIBLE);
            adapter.clear();
            return;
        }
        noItem.setVisibility(View.GONE);
        adapter.clear();
        adapter.addAll(news);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<News>> loader) {
        adapter.clear();
    }

    public static class AboutDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.about_text))
                    .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            return builder.create();
        }
    }

    public static class ContactDialogFragment extends DialogFragment {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = getActivity().getLayoutInflater().inflate(R.layout.contact, null);
            final EditText user = view.findViewById(R.id.user);
            final EditText feed = view.findViewById(R.id.feed);
            builder.setTitle("Feedback")
                    .setView(view)
                    .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("*/*");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"caothivananh98@gmail.com"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for BookSearch App from " + user.getText().toString());
                            intent.putExtra(Intent.EXTRA_TEXT, feed.getText().toString());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }
                            dialogInterface.cancel();
                        }
                    });
            return builder.create();
        }
    }

}
