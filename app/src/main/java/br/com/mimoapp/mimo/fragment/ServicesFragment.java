package br.com.mimoapp.mimo.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.activity.ServiceActivity;
import br.com.mimoapp.mimo.adapter.ServiceAdapter;
import br.com.mimoapp.mimo.config.FireBaseConfig;
import br.com.mimoapp.mimo.model.Service;

import static android.app.Activity.RESULT_OK;

/**
 * <h1> Content description </h1>
 * <ul>
     * <li> Inflate layout fragment </li>
     * <li> Get progress spinner </li>
     * <li> Get Search bar </li>
     * <li> Get list view </li>
     * <li> Get float action button </li>
     * <li> Get EditText input of Search </li>
     * <li> Set icon of EditText </li>
     * <li> Set border to search bar if SDK is to old for show elevation </li>
     * <li> Configure services listView adapter </li>
     * <li> Get node "services" from FireBase </li>
     * <li> Set listener of data change in FireBase </li>
     * <li> Get result of search in FireBase then export to </li>
     * <li> Reset services array to comport the new search </li>
     * <li> Iterate retrieved data </li>
     * <li> Get data from FireBase then assign to services objects array </li>
     * <li> Notify adapter (update listView) </li>
     * <li> Hide progress spinner </li>
     * <li> Set action of item list click </li>
     * <li> On item click, start a intent of service </li>
     * <li> Create new intent of activity </li>
     * <li> Pass service data to new intent of activity </li>
     * <li> Start activity "service" </li>
     * <li> Set on typing listener </li>
     * <li> While typing event listener </li>
     * <li> If count of characters in EditText is greater than zero </li>
     * <li> Get string term to be search in "services" array </li>
     * <li> Set temporary local array of "Service" object </li>
     * <li> Setup iterator for services array ( @link: https://developer.android.com/reference/java/util/Iterator.html ) </li>
     * <li> Iterate array of objects "Service" </li>
     * <li> Get current object </li>
     * <li> Get data from current object of iteration </li>
     * <li> Set pattern to be search (content of EditText received on each character type) </li>
     * <li> Match pattern with string currentName </li>
     * <li> Set pattern to be search (content of EditText received on each character type) </li>
     * <li> Match pattern with string currentName </li>
     * <li> Check result of match pattern with string </li>
     * <li> If true, add current object "service" to temporary array of objects </li>
     * <li> Assign the new array of objects created through the search to services adapter </li>
     * <li> Update adapter with new data </li>
     * <li> Set onclick listener to float action button hear voice </li>
     * <li> Create new intent to captre the voice </li>
     * <li> Setup language of recognizer </li>
     * <li> Setup text alert of capture frame </li>
     * <li> Test if device can run a voice capture </li>
     * <li> If device cannot run voice capture, show alert and get focus on EditText </li>
     * <li> On scroll event listener </li>
     * <li> Check if scrolled up or down </li>
     * <li> Hide search bar if scroll down </li>
     * <li> Hide FAB if scroll down </li>
     * <li> Show search bar if scroll up </li>
     * <li> Show search bar if scroll up </li>
     * <li> Handle result of voice recognizer </li>
     * <li> Result of user voice capture </li>
     * <li> Put result of user voice capture in EditeText of search </li>
     * <li> Remove especial characters entities </li>
     * <li> Open fireBase listener when app is open and this fragment on screen </li>
     * <li> Close fireBase listener when fragment change or app lose focus (minimized) </li>
 * </ul>
 */

public class ServicesFragment extends Fragment {

    private final int REQ_CODE_SPEECH_INPUT = 7;

    private ProgressBar servicesProgress;
    private EditText searchEditText;
    private FloatingActionButton fab;

    private ConstraintLayout searchBar;

    private ActionBar actionBar;

    private ListView listView;
    private ArrayAdapter<Service> adapter;
    private ArrayList<Service> services;

    private DatabaseReference fireBase;

    private String KEY_SERVICE_NODE = "services";
    private String KEY_ID = "service_id";
    private String KEY_NAME = "name";
    private String KEY_DESCRIPTION = "description";

    private ValueEventListener valueEventListenerServices;

    public ServicesFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        // Inflate layout fragment
        View view = inflater.inflate(R.layout.fragment_services, container, false);


        // Get progress spinner
        servicesProgress = view.findViewById(R.id.servicesProgress);

        // Get Search bar
        searchBar = view.findViewById(R.id.search_bar);

        // Get list view
        listView = view.findViewById(R.id.lvServices);

        // Get float action button
        fab = getActivity().findViewById(R.id.voiceSearchFab);

        // Get created toolbar
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        // Get EditText input of Search
        searchEditText = view.findViewById(R.id.searchEditText);

        // Set icon of EditText
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search_black_24dp, 0);
        }

        // Set border to search bar if SDK is to old for show elevation
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            //searchBar.setBackgroundColor( getResources().getColor( R.color.secondaryLighterColor ) );
        }

        // Configure services listView adapter
        services = new ArrayList<>();
        adapter = new ServiceAdapter( getActivity(), services );
        listView.setAdapter( adapter );

        // Get node "services" from FireBase
        fireBase = FireBaseConfig.getFirebase().child( KEY_SERVICE_NODE );

        // Set listener of data change in FireBase
        valueEventListenerServices = new ValueEventListener() {

            /**
             * Get result of search in FireBase then export to
             * array ArrayList<Service> ( services ) to serve the adapter to compose the list
             * @param dataSnapshot FireBase retrieved data
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Reset services array to comport the new search
                services.clear();

                // Iterate retrieved data
                for (DataSnapshot data: dataSnapshot.getChildren() ) {

                    // Get data from FireBase then assign to services objects array
                    Service service = data.getValue( Service.class );
                    services.add( service );

                }

                // Notify adapter (update listView)
                adapter.notifyDataSetChanged();

                // Hide progress spinner
                servicesProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Set action of item list click
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            /**
             * On item click, start a intent of service
             * @param position current item
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Create new intent of activity
                Intent i = new Intent(getActivity(), ServiceActivity.class);
                Service service = services.get( position );

                // Pass service data to new intent of activity
                i.putExtra(KEY_ID, service.getId());
                i.putExtra(KEY_NAME, service.getName());
                i.putExtra(KEY_DESCRIPTION, service.getDescription());

                // Start activity "service"
                startActivity(i);

            }
        } );

        // Set on typing listener
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            // While typing event listener
            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {

                // If count of characters in EditText is greater than zero
                if (count > 0) {

                    // Get string term to be search in "services" array
                    String strSearch = standardizeString( s.toString() );

                    // Set temporary local array of "Service" object
                    ArrayList<Service> tmpServices = new ArrayList<>();

                    // Setup iterator for services array ( @link: https://developer.android.com/reference/java/util/Iterator.html )
                    Iterator<Service> servicesIterator = services.iterator();

                    // Iterate array of objects "Service"
                    while ( servicesIterator.hasNext() ) {

                        // Get current object
                        Service currService = servicesIterator.next();

                        // Get data from current object of iteration
                        String currentName = standardizeString( currService.getName() );
                        String currentDescription = standardizeString( currService.getDescription() );

                        // Unify name with description to search
                        currentName = currentName + " " + currentDescription;

                        //Set pattern to be search (content of EditText received on each character type)
                        Pattern MY_PATTERN_0 = Pattern.compile( strSearch );

                        // Match pattern with string currentName
                        Matcher nameMatcher = MY_PATTERN_0.matcher( currentName );

                        // Check result of match pattern with string
                        if ( nameMatcher.find() ) {
                            // If true, add current object "service" to temporary array of objects
                            tmpServices.add( currService );
                        }
                    }

                    // Assign the new array of objects created through the search to services adapter
                    adapter = new ServiceAdapter( getActivity(), tmpServices );

                    // Update adapter with new data
                    listView.setAdapter( adapter );

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Set onclick listener to float action button hear voice
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create new intent to captre the voice
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                // Setup language of recognizer
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                // Setup text alert of capture frame
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search));

                // Test if device can run a voice capture
                try {

                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);

                } catch (ActivityNotFoundException a) {

                    // If device cannot run voice capture, show alert and get focus on EditText
                    Toast.makeText(getContext(), R.string.voice_search_error, Toast.LENGTH_SHORT).show();
                    //searchEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput( getView(), 1);

                }

            }

        });

        return view;
    }

    /**
     * Handle result of voice recognizer
     * @param data voice capture result in array of strings
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    // Result of user voice capture
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    // Put result of user voice capture in EditeText of search
                    searchEditText.setText(result.get(0));
                }
                break;
            }
        }
    }

    /**
     * Remove especial characters entities
     * @param str String to be standardized
     * @return standardized string
     */
    public String standardizeString(String str) {

        str = str.toLowerCase()
                .replaceAll(",", "")
                .replaceAll("ç", "c")
                .replaceAll("á", "a")
                .replaceAll("à", "a")
                .replaceAll("ã", "a")
                .replaceAll("â", "a")
                .replaceAll("é", "e")
                .replaceAll("è", "e")
                .replaceAll("ẽ", "e")
                .replaceAll("ê", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ò", "o")
                .replaceAll("õ", "o")
                .replaceAll("ô", "o")
                .replaceAll("ú", "u")
                .replaceAll("ù", "u")
                .replaceAll("ũ", "u")
                .replaceAll("û", "u")
                .replaceAll(" ", "|")
                .replaceAll("  ", "|")
                .replaceAll("   ", "|");
        return "("+str +")";
    }

    /**
     * Open fireBase listener when app is open and this fragment on screen
     */
    @Override
    public void onStart() {
        super.onStart();
        fireBase.addValueEventListener( valueEventListenerServices );
    }

    /**
     * Close fireBase listener when fragment change or app lose focus (minimized)
     */
    @Override
    public void onStop() {
        super.onStop();
        fireBase.removeEventListener( valueEventListenerServices );
    }
}
