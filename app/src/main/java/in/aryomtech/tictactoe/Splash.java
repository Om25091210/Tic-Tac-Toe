package in.aryomtech.tictactoe;

import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.util.List;

import in.aryomtech.tictactoe.fcm.topic;
import soup.neumorphism.NeumorphButton;
import www.sanju.motiontoast.MotionToast;

public class Splash extends AppCompatActivity {

    Dialog dialog,google_dialog;
    Uri deep_link_uri;
    NeumorphButton create,join;
    String device_token;
    LinearLayout sign_in;
    String temp_uid;
    FirebaseAuth auth;
    LinearLayout invite;
    String strProvider;
    FirebaseUser user;
    GoogleSignInClient agooglesigninclient;
    private static final int RC_SIGN_IN = 101;
    DatabaseReference user_reference;
    private AppUpdateManager mAppUpdateManager;
    private final int RC_APP_UPDATE = 999;
    private int inAppUpdateType;
    private com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // Creates instance of the manager.
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        // Returns an intent object that you use to check for an update.
        appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        //lambda operation used for below listener
        //For flexible update
        installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        };
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        //For Immediate
        inAppUpdateType = AppUpdateType.IMMEDIATE; //1
        inAppUpdate();

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        user_reference=FirebaseDatabase.getInstance().getReference().child("users");

        auth.getAccessToken(false).addOnSuccessListener(getTokenResult -> {
            strProvider = getTokenResult.getSignInProvider();
            if(strProvider!=null){
                if(strProvider.equals("anonymous"))
                    sign_in.setVisibility(View.VISIBLE);
                else
                    sign_in.setVisibility(View.GONE);
            }
        });

        MobileAds.initialize(this, initializationStatus -> {
        });

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/myTopic3")
                .addOnCompleteListener(task -> {
                    String msg = "Done";
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                    }
                    Log.d("topic_log", msg);
                    Toast.makeText(Splash.this, msg, Toast.LENGTH_SHORT).show();
                });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                mAdView.loadAd(adRequest);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });


        sign_in=findViewById(R.id.sign_in);
        join=findViewById(R.id.join);
        join.setOnClickListener(view -> {
            Intent intent = new Intent(Splash.this,play_offline.class);
            startActivity(intent);
        });
        invite=findViewById(R.id.imageView3);
        invite.setOnClickListener(v->{
            String title ="*Do you know about this app \uD83D\uDE0E*"+"\n\n"+"Accept my challenge to play *tic-tac-toe* online by *clicking* on the link below."; //Text to be shared
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, title+"\n\n"+"This is a playstore link to download.. " + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        });
        if(user==null){
            auth.signInAnonymously().addOnCompleteListener(task -> {

                MotionToast.Companion.darkColorToast(Splash.this,
                        "Welcome \uD83D\uDE03",
                        "Signed in - Anonymously.",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(Splash.this, R.font.helvetica_regular));

                user=auth.getCurrentUser();
                user_reference.child(user.getUid()).child("uid").setValue(user.getUid());
                user_reference.child(user.getUid()).child("token").setValue(device_token);

            });
        }

        get_device_token();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        agooglesigninclient = GoogleSignIn.getClient(this,gso);

        sign_in.setOnClickListener(v-> signIn_Google());

        deep_link_uri = getIntent().getData();//deep link value
        if(deep_link_uri!=null){
            if (deep_link_uri.toString().equals("https://tic-tac-toe-online")){
                Toast.makeText(Splash.this, "wrong link 1", Toast.LENGTH_SHORT).show();
            }
            else if(deep_link_uri.toString().equals("http://tic-tac-toe-online")){
                Toast.makeText(Splash.this, "wrong link 2", Toast.LENGTH_SHORT).show();
            }
            else if(deep_link_uri.toString().equals("tic-tac-toe-online")){
                Toast.makeText(Splash.this, "wrong link 3", Toast.LENGTH_SHORT).show();
            }
            else{
                // if the uri is not null then we are getting the
                // path segments and storing it in list.
                List<String> parameters = deep_link_uri.getPathSegments();
                // after that we are extracting string from that parameters.
                if(parameters!=null) {
                    if(parameters.size()>1) {
                        String param = parameters.get(parameters.size() - 1);
                        String uid = parameters.get(parameters.size() - 2);
                        // on below line we are setting
                        // that string to our text view
                        // which we got as params.
                        Log.e("deep_link_value", param + "");
                        Log.e("deep_link_value_uid", uid + "");
                        Intent intent = new Intent(Splash.this,pick_your_side.class);
                        intent.putExtra("key_for_room",param);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(Splash.this, "Wrong link 4", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("roomID");


        create=findViewById(R.id.create);
        create.setOnClickListener(v->{
            String push_key=reference.push().getKey();
            dialog = new Dialog(Splash.this);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.create_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            TextView link_text=dialog.findViewById(R.id.textView6);
            String text_link="https://tic-tac-toe-online.com/aryomtech/"+push_key;
            link_text.setText(text_link);
            ImageView cross=dialog.findViewById(R.id.imageView6);

            cross.setOnClickListener(v1-> dialog.dismiss());

            Button share=dialog.findViewById(R.id.share);
            share.setOnClickListener(v2->{
                Intent intent=new Intent(Splash.this,pick_your_side.class);
                intent.putExtra("key_for_room",push_key);
                startActivity(intent);

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("link ",text_link);
                clipboard.setPrimaryClip(clip);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    String title ="*2 min challenge for you \uD83D\uDE0E*"+"\n\n"+"Accept my challenge to play *tic-tac-toe* online by *clicking* on the link below."; //Text to be shared
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, title+"\n\n" +text_link+"\n\n"+"This is a playstore link to download.. " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                }, 1000);

            });
        });
    }

    private void get_device_token() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e("error","device token fetching failed");
                        return;
                    }

                    device_token = task.getResult();

                });
    }

    private void signIn_Google() {
        Intent SignInIntent = agooglesigninclient.getSignInIntent();
        startActivityForResult(SignInIntent,RC_SIGN_IN);
        google_dialog = new Dialog(Splash.this);
        google_dialog.setCancelable(true);
        google_dialog.setContentView(R.layout.loading);
        google_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        google_dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);


            } catch (ApiException e) {
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RC_APP_UPDATE) {
            //when user clicks update button
            if (resultCode == RESULT_OK) {
                Toast.makeText(Splash.this, "App download starts...", Toast.LENGTH_LONG).show();
            } else if (resultCode != RESULT_CANCELED) {
                //if you want to request the update again just call checkUpdate()
                Toast.makeText(Splash.this, "App download canceled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
                Toast.makeText(Splash.this, "App download failed.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        if(user!=null)
            temp_uid=user.getUid();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if(task.isSuccessful()){

                        google_dialog.dismiss();
                        user = auth.getCurrentUser();

                        assert user != null;

                        MotionToast.Companion.darkColorToast(Splash.this,
                                "Welcome \uD83D\uDE03",
                                "Signed in - Successfully.",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(Splash.this,R.font.helvetica_regular));
                        //String DeviceToken= FirebaseInstanceId.getInstance().getToken();
                    /*    String displayname;
                        String name = user.getDisplayName();

                        assert name != null;
                        displayname = name.replaceAll("[^a-zA-Z0-9]","");*/
                        user_reference.child(user.getUid()).child("email").setValue(user.getEmail());
                        user_reference.child(user.getUid()).child("uid").setValue(user.getUid());
                        user_reference.child(user.getUid()).child("name").setValue(user.getDisplayName());
                        user_reference.child(user.getUid()).child("token").setValue(device_token);
                        user_reference.child(temp_uid).removeValue();
                        updateUI();

                    }
                    else{
                        Toast.makeText(Splash.this, "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateUI() {

        sign_in.setVisibility(View.GONE);

    }
    private void inAppUpdate() {

        try {
            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(inAppUpdateType)) {
                    // Request the update.

                    try {
                        mAppUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                inAppUpdateType,
                                // The current activity making the update request.
                                Splash.this,
                                // Include a request code to later monitor this update request.
                                RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException ignored) {

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void popupSnackbarForCompleteUpdate() {
        try {
            Snackbar snackbar =
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "An update has just been downloaded.\nRestart to update",
                            Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction("INSTALL", view -> {
                if (mAppUpdateManager != null){
                    mAppUpdateManager.completeUpdate();
                }
            });
            snackbar.setActionTextColor(ResourcesCompat.getColor(getResources(),R.color.green_A400,null));
            snackbar.show();

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        try {
            mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    try {
                        mAppUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                inAppUpdateType,
                                this,
                                RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            });


            mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                //For flexible update
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onResume();
    }

}