package com.example.android.booksearch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Book>>, Observer{
    private static final String BOOK_LIST = "book_list";
    private static final String SORT_FRAGMENT = "sort_fragment";
    private static final String ABOUT_FRAGMENT = "about_fragment";
    private static final String CONTACT_FRAGMENT = "contact_fragment";
    private static final String NO_SORT = "no_sort" ;
    private static BookAdapter bookAdapter;
    private LoaderManager loaderManager;
    private ConnectivityManager connectivityManager;
    private TextView emptyTextView;
    private ImageView emptyImageView;
    private RelativeLayout empty_view;
    private ProgressBar loading;
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private EditText editText;
    private Button search;
    private int maxResult = 20;
    private String url;
    private static BookVariableWrapper sortMethod;
    private final int LOADER_ID = 1;
    public final static String FILTER_BY_EBOOK = "Filter by ebook";
    public final static String FILTER_BY_PDF = "Filter by pdf";
    public final static String SORT_BY_DATE = "Sort by early publish date";
    public final static String FILTER_BY_LANGUAGE = "Filter by english";
    private static String[] sortMethods = {FILTER_BY_EBOOK, FILTER_BY_PDF, SORT_BY_DATE, FILTER_BY_LANGUAGE};

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BOOK_LIST, getListFromAdapter());
    }

    private static ArrayList<Book> getListFromAdapter() {
        ArrayList<Book> list = new ArrayList<>();
        for (int i = 0; i < bookAdapter.getCount(); i++) {
            list.add(bookAdapter.getItem(i));
        }
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set change observer
        sortMethod= new BookVariableWrapper(NO_SORT);
        sortMethod.addObserver(this);

        //Get booklist
        ListView bookList = findViewById(R.id.list);
        loaderManager = getLoaderManager();

        if (savedInstanceState != null) {
            ArrayList<Book> list = savedInstanceState.getParcelableArrayList(BOOK_LIST);
            bookAdapter = new BookAdapter(this, list);
        } else {
            bookAdapter = new BookAdapter(this, new ArrayList<Book>());
        }
        bookList.setAdapter(bookAdapter);

        emptyTextView = findViewById(R.id.text);
        emptyImageView = findViewById(R.id.image);
        empty_view = findViewById(R.id.empty_view);
        loading = findViewById(R.id.loading);
        editText = findViewById(R.id.search);
        search = findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable searchKey = editText.getText();
                if(searchKey.toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter keyword to search", Toast.LENGTH_LONG).show();
                }else{
                    url = BASE_URL + searchKey.toString() + "&" + "maxResults=" + maxResult;
                    //check network and call request
                    callInBackGround();
                }
            }
        });

        //Create toolbar set up
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void callInBackGround() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            setOnInternet();
            if (loaderManager.getLoader(LOADER_ID) == null) {
                //init
                loaderManager.initLoader(LOADER_ID, null, MainActivity.this);
            } else {
                //restart
                loaderManager.restartLoader(LOADER_ID, null, MainActivity.this);
            }
        } else {
            bookAdapter.clear();
            setNoInternet();
        }
    }

    /**
     * inflate xml menu to real menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View view = findViewById(R.id.setting_button);
        switch (item.getItemId()) {
            case R.id.setting_button:
                //create pop up menu
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_items, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sort:
                                DialogFragment dialogFragment = new SortDialogFragment();
                                dialogFragment.show(getSupportFragmentManager(), SORT_FRAGMENT);
                                break;
                            case R.id.about:
                                DialogFragment dialogFragment1 = new AboutDialogFragment();
                                dialogFragment1.show(getSupportFragmentManager(), ABOUT_FRAGMENT);
                                break;
                            case R.id.setting:
                                break;
                            case R.id.feedback:
                                DialogFragment dialogFragment2 = new ContactDialogFragment();
                                dialogFragment2.show(getSupportFragmentManager(), CONTACT_FRAGMENT);
                                break;
                            case R.id.saved:
                                //Save for next lession
                                break;
                            default:
                                sortMethod.setSorthMethod(NO_SORT);
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setNoInternet() {
        loading.setVisibility(GONE);
        empty_view.setVisibility(View.VISIBLE);
        emptyTextView.setText("There is no internet connection");
        emptyImageView.setImageResource(R.drawable.ic_cloud_off_black_24dp);
    }

    private void setOnInternet() {
        empty_view.setVisibility(GONE);
    }

    @Override
    public Loader<ArrayList<Book>> onCreateLoader(int i, Bundle bundle) {
        loading.setVisibility(View.VISIBLE);
        return new BookLoader(MainActivity.this, url,sortMethod.getSorthMethod());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Book>> loader, ArrayList<Book> books) {
        loading.setVisibility(View.GONE);
        if ((books == null) || books.isEmpty()) {
            emptyTextView.setText("There is no book in the searching.");
            return;
        }
        bookAdapter.clear();
        bookAdapter.addAll(books);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Book>> loader) {
        bookAdapter.clear();
    }

    @Override
    public void update(Observable observable, Object o) {
        callInBackGround();
    }
    //class Dialog Fragment
    public static class SortDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(sortMethods, new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sortMethod.setSorthMethod(sortMethods[i]);
                    dialogInterface.cancel();
                }
            });
            return builder.create();
        }
    }

    public static class AboutDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This application is one of the project of the nanodegree course Networking." +
                    "\nAuthor: L3I2")
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
            EditText user = view.findViewById(R.id.user);
            EditText feed = view.findViewById(R.id.feed);
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
                            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                            dialogInterface.cancel();
                        }
                    });
            return builder.create();
        }
    }

}
