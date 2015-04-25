package com.labtalk.evgenijavstein.oranges4you;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;



public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
         {
    /* Google API Client: communicates with the Google-Sign-In API & Google Play Services */
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 0;
    private boolean mIntentInProgress;
    private boolean mSignInClicked = false;

    private static final String tag = MainActivity.class.getCanonicalName();
    SignInButton signInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope("profile"))//integrate Google services: e.g. Drive and Games using scopes
                .build();

        signInButton=(SignInButton)findViewById(R.id.sign_in_button);
        signInButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInClicked = true;

                //TRIGGERS AUTHENTICATION & AUTHORISATION (Scope: profile)
                mGoogleApiClient.connect();
            }
        });


    }

    @Override
    public void onConnected(Bundle bundle) {
        //AT THIS POINT THE USER IS AUTHENTICATED


        signInButton.setVisibility(View.GONE);
        mSignInClicked = false;

        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
            @Override
            public void onResult(People.LoadPeopleResult loadPeopleResult) {

                //REQUEST RESOURCES FROM ALLOWED SCOPE
                if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                    Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                    ((TextView) findViewById(R.id.name)).setText(person.getDisplayName());
                    ((TextView) findViewById(R.id.language)).setText(person.getLanguage());
                    ((TextView) findViewById(R.id.url)).setText(person.getUrl());
                    ((TextView) findViewById(R.id.image_url)).setText(person.getImage().getUrl());
                    findViewById(R.id.gridContent).setVisibility(View.VISIBLE);
                }
            }
        });



    }


    @Override
    public void onConnectionSuspended(int i) {

            mGoogleApiClient.connect();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //CONNECT ON START UP: IF SUCCESS -> SIGN-IN BUTTON IS HIDDEN, USER DATA DISPLAYED
        mGoogleApiClient.connect();


    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }




}
