package in.aryomtech.tictactoe;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import in.aryomtech.tictactoe.fcm.Specific;
import in.aryomtech.tictactoe.fcm.topic;
import soup.neumorphism.NeumorphButton;


public class MainActivity extends AppCompatActivity {

    Animation animation1;
    String which_user="";
    FirebaseAuth auth;
    NeumorphButton share;
    FirebaseUser user;
    TextView user2_name,user1_name,user_wins,opponent_wins;
    Dialog dialog;
    String turn="Start the game";
    String push_key;
    TextView turn_text;
    boolean once=true;
    static List<String> winning_positions;
    static int[][] positions=new int[504][3];
    List<Integer> user1=new ArrayList<>();
    String sign="",name_of_user;
    String token_for_user,opponent_name;
    DatabaseReference reference;
    boolean show_draw=true;
    private AdView mAdView;
    ValueEventListener listener;
    List<Long> marked_list=new ArrayList<>();
    ConstraintLayout constraintLayout;
    ValueEventListener valueEventListener;
    ImageView img1,img2,img3,img4,img5,img6,img7,img8,img9;
    MediaPlayer ring_for_user,ring_for_opponent,win_sound,lose_sound,draw_sound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.white));

        animation1 = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.bounce);

        MobileAds.initialize(this, initializationStatus -> {
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

        ring_for_opponent= MediaPlayer.create(MainActivity.this,R.raw.user_click_sound);
        ring_for_user= MediaPlayer.create(MainActivity.this,R.raw.opponent_click_sound);

        win_sound= MediaPlayer.create(MainActivity.this,R.raw.win_sound);
        lose_sound= MediaPlayer.create(MainActivity.this,R.raw.lose_sound);
        draw_sound= MediaPlayer.create(MainActivity.this,R.raw.draw_sound);

        sign=getIntent().getStringExtra("picked_choice");

        which_user=getIntent().getStringExtra("which_user_?");

        reference= FirebaseDatabase.getInstance().getReference().child("roomID");

        push_key=getIntent().getStringExtra("key_for_roomID");

        name_of_user=getIntent().getStringExtra("name_of_user");

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        winning_positions=new ArrayList<>();
        winning_positions.add("[1, 2, 3]");
        winning_positions.add("[1, 4, 7]");
        winning_positions.add("[1, 5, 9]");
        winning_positions.add("[4, 5, 6]");
        winning_positions.add("[2, 5, 8]");
        winning_positions.add("[3, 5, 7]");
        winning_positions.add("[7, 8, 9]");
        winning_positions.add("[3, 6, 9]");

        img1=findViewById(R.id.img1);
        img2=findViewById(R.id.img2);
        img3=findViewById(R.id.img3);
        img4=findViewById(R.id.img4);
        img5=findViewById(R.id.img5);
        img6=findViewById(R.id.img6);
        img7=findViewById(R.id.img7);
        img8=findViewById(R.id.img8);
        img9=findViewById(R.id.img9);

        user2_name=findViewById(R.id.user2_name);
        user1_name=findViewById(R.id.user1_name);
        user_wins=findViewById(R.id.textView);
        opponent_wins=findViewById(R.id.textView3);
        constraintLayout=findViewById(R.id.constraint);
        turn_text=findViewById(R.id.textView9);
        share=findViewById(R.id.share);
        turn_text.setText(turn);

        new Handler(Looper.myLooper()).postDelayed(() -> reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(which_user).exists()){
                    if(!Objects.equals(snapshot.child(which_user).getValue(String.class), user.getUid())){
                        if(which_user.equals("user1")){
                            which_user="user2";
                            reference.child(push_key).child(which_user).setValue(user.getUid());
                        }
                        else{
                            which_user="user1";
                            reference.child(push_key).child(which_user).setValue(user.getUid());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        }),500);

        share.setOnClickListener(v->{
            String text_link="https://tic-tac-toe-online.com/aryomtech/"+push_key;
            String title ="*2 min challenge for you \uD83D\uDE0E*"+"\n\n"+"Accept my challenge to play *tic-tac-toe* online by *clicking* on the link below."; //Text to be shared
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, title+"\n\n" +text_link+"\n\n"+"This is a playstore link to download.. " + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        });

        assert push_key != null;
        reference.child(push_key).child("match").setValue("ongoing");
        listener= reference.child(push_key).child("marks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (!user1.contains(Integer.parseInt(Objects.requireNonNull(ds.getKey())))) {
                            play(Integer.parseInt(ds.getKey()), "user2");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        reference.child(push_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(which_user.equals(snapshot.child("turn").getValue(String.class))){
                    String text="Tap its your turn";
                    turn_text.setText(text);
                }
                else if(!snapshot.child("turn").exists()){
                    turn_text.setText(turn);
                }
                else{
                    String text="Wait opponent is choosing";
                    turn_text.setText(text);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        load_check_data();
        
        img1.setOnClickListener(v-> check_for_user_online(1));
        img2.setOnClickListener(v-> check_for_user_online(2));
        img3.setOnClickListener(v-> check_for_user_online(3));
        img4.setOnClickListener(v-> check_for_user_online(4));
        img5.setOnClickListener(v-> check_for_user_online(5));
        img6.setOnClickListener(v-> check_for_user_online(6));
        img7.setOnClickListener(v-> check_for_user_online(7));
        img8.setOnClickListener(v-> check_for_user_online(8));
        img9.setOnClickListener(v-> check_for_user_online(9));

        check_for_lose();
        check_for_replay();
        check_for_draw();
    }

    private void check_for_draw() {
        reference.child(push_key).child("draw").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && show_draw){
                    show_draw();
                    show_draw=false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void show_draw() {
        dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_lose_dialog);
        LottieAnimationView lottieAnimationView=dialog.findViewById(R.id.trophy);
        LottieAnimationView lottieAnimationView1=dialog.findViewById(R.id.animation_view);
        lottieAnimationView1.setVisibility(View.GONE);
        lottieAnimationView.setAnimation("tie.json");
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        TextView msg=dialog.findViewById(R.id.textView2);
        String text="Match tie";
        msg.setText(text);
        TextView play_again=dialog.findViewById(R.id.msg);
        play_again.setOnClickListener(v->
        {
            if(token_for_user!=null) {
                Specific specific = new Specific();
                specific.noti(name_of_user, "Wants to play again. let's win it.", token_for_user);
            }
            reference.child(push_key).child("match").setValue("replay");
            clear_values();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!MainActivity.this.isFinishing() && !dialog.isShowing()) {
            draw_sound.start();
            dialog.show();
        }
    }

    private void load_check_data() {
        if(which_user.equals("user1")){
            reference.child(push_key).child("user2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String name="guest";
                        user2_name.setText(name);
                        get_uid("user2");
                        share.setVisibility(View.GONE);
                        user2_name.setTextColor(Color.parseColor("#02C39A"));
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Opponent online \uD83D\uDE0E, Let's Win", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.setBackgroundTint(Color.parseColor("#245D6B"));
                        snackbar.show();
                    }
                    else{
                        String name="Not in room";
                        user2_name.setText(name);
                        user2_name.setTextColor(Color.parseColor("#FF4359"));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        else{
            reference.child(push_key).child("user1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String name="guest";
                        get_uid("user1");
                        user2_name.setText(name);
                        share.setVisibility(View.GONE);
                        user2_name.setTextColor(Color.parseColor("#02C39A"));
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Opponent online \uD83D\uDE0E, Let's Win", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.setBackgroundTint(Color.parseColor("#245D6B"));
                        snackbar.show();
                    }
                    else{
                        String name="Not in room";
                        user2_name.setText(name);
                        user2_name.setTextColor(Color.parseColor("#FF4359"));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void get_uid(String user) {
        reference.child(push_key).child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.exists()){
                       get_name(snapshot.getValue(String.class));
                   }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void get_name(String uid){
        DatabaseReference user_ref=FirebaseDatabase.getInstance().getReference().child("users");
        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(uid).child("name").exists()){
                    opponent_name=snapshot.child(uid).child("name").getValue(String.class);
                    user2_name.setText(opponent_name);
                }
                else{
                    opponent_name="guest";
                }
                if(snapshot.child(uid).child("token").exists()){
                    token_for_user=snapshot.child(uid).child("token").getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void check_for_lose() {
        valueEventListener=reference.child(push_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("won").exists()){
                    if(!Objects.equals(snapshot.child("won").getValue(String.class), which_user)) {
                        if (snapshot.child("win_positions").exists()) {
                            if (once) {
                                once=false;
                                set_line(Objects.requireNonNull(snapshot.child("win_positions").getValue(String.class)));
                                new Handler(Looper.myLooper()).postDelayed(() -> set_lose_data(), 700);
                            }
                        }
                        else{
                            if(marked_list.size()==9 && show_draw) {
                                show_draw();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void set_line(String positions) {
        if(positions.equals(winning_positions.get(0))){
            img1.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img2.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img3.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(winning_positions.get(1))){
            img1.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img4.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img7.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
        else if(positions.equals(winning_positions.get(2))){
            img1.setBackgroundResource(R.drawable.ic_left_dialog);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_left_dialog),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_left_dialog),400);
        }
        else if(positions.equals(winning_positions.get(3))){
            img4.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img6.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(winning_positions.get(4))){
            img2.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img8.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
        else if(positions.equals(winning_positions.get(5))){
            img3.setBackgroundResource(R.drawable.ic_right_diagonal);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_right_diagonal),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img7.setBackgroundResource(R.drawable.ic_right_diagonal),400);
        }
        else if(positions.equals(winning_positions.get(6))){
            img7.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img8.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(winning_positions.get(7))){
            img3.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img6.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
    }

    private void set_lose_data() {
        dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_lose_dialog);
        LottieAnimationView lottieAnimationView=dialog.findViewById(R.id.trophy);
        LottieAnimationView lottieAnimationView1=dialog.findViewById(R.id.animation_view);
        lottieAnimationView1.setVisibility(View.GONE);
        lottieAnimationView.setAnimation("loose.json");
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        TextView msg=dialog.findViewById(R.id.textView2);
        String text="You lose";
        msg.setText(text);
        TextView play_again=dialog.findViewById(R.id.msg);
        play_again.setOnClickListener(v->
        {
            if(token_for_user!=null) {
                Specific specific = new Specific();
                specific.noti(name_of_user, "Wants to play again. let's win it.", token_for_user);
            }
            reference.child(push_key).child("match").setValue("replay");
            clear_values();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!MainActivity.this.isFinishing() && !dialog.isShowing()) {
            lose_sound.start();
            dialog.show();
        }
        update();
    }

    private void update() {
        reference.child(push_key).child("wins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(which_user.equals("user1")){
                    if(snapshot.child("user2").exists()){
                        String value=snapshot.child("user2").getValue(Long.class)+"";
                        opponent_wins.setText(value);
                    }
                    if(snapshot.child("user1").exists()){
                        String value=snapshot.child("user1").getValue(Long.class)+"";
                        user_wins.setText(value);
                    }
                }
                else{
                    if(snapshot.child("user2").exists()){
                        String value=snapshot.child("user2").getValue(Long.class)+"";
                        user_wins.setText(value);
                    }
                    if(snapshot.child("user1").exists()){
                        String value=snapshot.child("user1").getValue(Long.class)+"";
                        opponent_wins.setText(value);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void check_for_user_online(int i) {
        reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child("turn").exists() || Objects.equals(snapshot.child("turn").getValue(String.class), which_user)) {
                    if (which_user.equals("user1")) {
                        if (snapshot.child("user2").exists()) {
                            play(i, "user1");
                        }
                    }
                    else {
                        if (snapshot.child("user1").exists()) {
                            play(i, "user1");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void play(int image,String user){
        if(image==1){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(1);
                    marked_list.add(1L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img1.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("1").setValue(which_user);
                        img1.setImageResource(R.drawable.ic_circle);
                    } else {
                        img1.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("1").setValue(which_user);
                        img1.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(1L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img1.startAnimation(animation1);
                        img1.setImageResource(R.drawable.ic_cross);
                    } else {
                        img1.startAnimation(animation1);
                        img1.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img1.setAnimation(null),1000);
            }
        }
        else if(image==2){
            if(!marked_list.contains((long)image)) {
                if(user.equals("user1")) {
                    user1.add(2);
                    marked_list.add(2L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img2.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("2").setValue(which_user);
                        img2.setImageResource(R.drawable.ic_circle);
                    } else {
                        img2.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("2").setValue(which_user);
                        img2.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(2L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img2.startAnimation(animation1);
                        img2.setImageResource(R.drawable.ic_cross);
                    } else {
                        img2.startAnimation(animation1);
                        img2.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img2.setAnimation(null),1000);
            }
        }
        else if(image==3){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(3);
                    marked_list.add(3L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img3.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("3").setValue(which_user);
                        img3.setImageResource(R.drawable.ic_circle);
                    } else {
                        img3.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("3").setValue(which_user);
                        img3.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(3L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img3.startAnimation(animation1);
                        img3.setImageResource(R.drawable.ic_cross);
                    } else {
                        img3.startAnimation(animation1);
                        img3.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img3.setAnimation(null),1000);
            }
        }
        else if(image==4){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(4);
                    marked_list.add(4L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img4.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("4").setValue(which_user);
                        img4.setImageResource(R.drawable.ic_circle);
                    } else {
                        img4.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("4").setValue(which_user);
                        img4.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(4L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img4.startAnimation(animation1);
                        img4.setImageResource(R.drawable.ic_cross);
                    } else {
                        img4.startAnimation(animation1);
                        img4.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img4.setAnimation(null),1000);
            }
        }
        else if(image==5){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(5);
                    marked_list.add(5L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img5.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("5").setValue(which_user);
                        img5.setImageResource(R.drawable.ic_circle);
                    } else {
                        img5.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("5").setValue(which_user);
                        img5.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(5L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img5.startAnimation(animation1);
                        img5.setImageResource(R.drawable.ic_cross);
                    } else {
                        img5.startAnimation(animation1);
                        img5.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img5.setAnimation(null),1000);
            }
        }
        else if(image==6){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(6);
                    marked_list.add(6L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img6.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("6").setValue(which_user);
                        img6.setImageResource(R.drawable.ic_circle);
                    } else {
                        img6.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("6").setValue(which_user);
                        img6.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(6L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img6.startAnimation(animation1);
                        img6.setImageResource(R.drawable.ic_cross);
                    } else {
                        img6.startAnimation(animation1);
                        img6.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img6.setAnimation(null),1000);
            }
        }
        else if(image==7){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(7);
                    marked_list.add(7L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img7.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("7").setValue(which_user);
                        img7.setImageResource(R.drawable.ic_circle);
                    } else {
                        img7.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("7").setValue(which_user);
                        img7.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(7L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img7.startAnimation(animation1);
                        img7.setImageResource(R.drawable.ic_cross);
                    } else {
                        img7.startAnimation(animation1);
                        img7.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img7.setAnimation(null),1000);
            }
        }
        else if(image==8){
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(8);
                    marked_list.add(8L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img8.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("8").setValue(which_user);
                        img8.setImageResource(R.drawable.ic_circle);
                    } else {
                        img8.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("8").setValue(which_user);
                        img8.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(8L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img8.startAnimation(animation1);
                        img8.setImageResource(R.drawable.ic_cross);
                    } else {
                        img8.startAnimation(animation1);
                        img8.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img8.setAnimation(null),1000);
            }
        }
        else{
            if(!marked_list.contains((long)image)){
                if(user.equals("user1")) {
                    user1.add(9);
                    marked_list.add(9L);
                    check_for_win(true);
                    if(which_user.equals("user1"))
                        reference.child(push_key).child("turn").setValue("user2");
                    else
                        reference.child(push_key).child("turn").setValue("user1");
                    ring_for_user.start();
                    if (sign.equals("o")) {
                        img9.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("9").setValue(which_user);
                        img9.setImageResource(R.drawable.ic_circle);
                    } else {
                        img9.startAnimation(animation1);
                        reference.child(push_key).child("marks").child("9").setValue(which_user);
                        img9.setImageResource(R.drawable.ic_cross);
                    }
                    if(marked_list.size()==9) {
                        if (check_for_win(false) == -1) {
                            show_draw();
                            show_draw=false;
                            reference.child(push_key).child("draw").setValue("draw");
                        }
                    }
                }
                else {
                    marked_list.add(9L);
                    ring_for_opponent.start();
                    if (sign.equals("o")) {
                        img9.startAnimation(animation1);
                        img9.setImageResource(R.drawable.ic_cross);
                    } else {
                        img9.startAnimation(animation1);
                        img9.setImageResource(R.drawable.ic_circle);
                    }
                }
                new Handler(Looper.myLooper()).postDelayed(()->img9.setAnimation(null),1000);
            }
        }
    }

    private int check_for_win(boolean enter) {
        for(int i=1;i<user1.size()+1;i++){
            for (int j=1;j<user1.size()+1;j++){
                if (j==i)
                    continue;
                for (int k=1;k<user1.size()+1;k++){
                    if (k==i || k==j)
                        continue;
                    positions[0][0]=user1.get(i-1);
                    positions[0][1]=user1.get(j-1);
                    positions[0][2]=user1.get(k-1);
                    if(winning_positions.contains(Arrays.toString(positions[0]))) {
                        reference.child(push_key).child("won").setValue(which_user);
                        reference.child(push_key).child("win_positions").setValue(Arrays.toString(positions[0]));
                        set_line(Arrays.toString(positions[0]));
                        if(enter)
                            new Handler(Looper.myLooper()).postDelayed(this::set_win_data,700);
                        return 1;
                    }
                }
            }
        }
        return -1;
    }

    private void set_win_data() {
        add_points();
        dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_lose_dialog);
        TextView play_again=dialog.findViewById(R.id.msg);
        play_again.setOnClickListener(v->
        {
            if(token_for_user!=null) {
                Specific specific = new Specific();
                specific.noti(name_of_user, "Wants to play again. let's win it.", token_for_user);
            }
            reference.child(push_key).child("match").setValue("replay");
            clear_values();
        });

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!MainActivity.this.isFinishing() && !dialog.isShowing()) {
            win_sound.start();
            dialog.show();
        }
        update();
    }

    private void add_points() {
        reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("wins").child(which_user).exists()){
                    Long win_points=snapshot.child("wins").child(which_user).getValue(Long.class);
                    assert win_points!=null;
                    reference.child(push_key).child("wins").child(which_user).setValue(win_points+1L);
                }
                else{
                    reference.child(push_key).child("wins").child(which_user).setValue(1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clear_values() {
        reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("turn").exists())
                    reference.child(push_key).child("turn").removeValue();
                if(snapshot.child("win_positions").exists())
                    reference.child(push_key).child("win_positions").removeValue();
                if(snapshot.child("won").exists())
                    reference.child(push_key).child("won").removeValue();
                if(snapshot.child("marks").exists())
                    reference.child(push_key).child("marks").removeValue();
                clear_data();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void clear_data() {
        //removing lines
        img1.setBackgroundResource(android.R.color.transparent);
        img2.setBackgroundResource(android.R.color.transparent);
        img3.setBackgroundResource(android.R.color.transparent);
        img4.setBackgroundResource(android.R.color.transparent);
        img5.setBackgroundResource(android.R.color.transparent);
        img6.setBackgroundResource(android.R.color.transparent);
        img7.setBackgroundResource(android.R.color.transparent);
        img8.setBackgroundResource(android.R.color.transparent);
        img9.setBackgroundResource(android.R.color.transparent);

        //clearing images
        img1.setImageResource(android.R.color.transparent);
        img2.setImageResource(android.R.color.transparent);
        img3.setImageResource(android.R.color.transparent);
        img4.setImageResource(android.R.color.transparent);
        img5.setImageResource(android.R.color.transparent);
        img6.setImageResource(android.R.color.transparent);
        img7.setImageResource(android.R.color.transparent);
        img8.setImageResource(android.R.color.transparent);
        img9.setImageResource(android.R.color.transparent);
        user1.clear();
        marked_list.clear();
        if(dialog!=null)
            dialog.dismiss();
    }
    private void check_for_replay(){
        reference.child(push_key).child("match").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(Objects.equals(snapshot.getValue(String.class), "replay")){
                        try {
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        reference.child(push_key).child("match").setValue("ongoing");
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Let's play again \uD83E\uDD17.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.setBackgroundTint(Color.parseColor("#046C95"));
                        snackbar.show();
                        clear_data();
                        new Handler(Looper.myLooper()).postDelayed(()->{
                            once=true;show_draw=true;
                            reference.child(push_key).child("draw").removeValue();
                        },1000);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Snackbar snackbar = Snackbar
                .make(constraintLayout, "Opponent left the game \uD83D\uDE15.", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setBackgroundTint(Color.parseColor("#245D6B"));
        snackbar.show();
        reference.child(push_key).child(which_user).removeValue();
        reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(which_user.equals("user1")){
                    if(!snapshot.child("user2").exists()){
                        reference.child(push_key).removeValue();
                    }
                }
                else{
                    if(!snapshot.child("user1").exists()){
                        reference.child(push_key).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Snackbar snackbar = Snackbar
                .make(constraintLayout, "Opponent left the game \uD83D\uDE15.", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setBackgroundTint(Color.parseColor("#245D6B"));
        snackbar.show();
        reference.child(push_key).child(which_user).removeValue();
        reference.child(push_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(which_user.equals("user1")){
                    if(!snapshot.child("user2").exists()){
                        reference.child(push_key).removeValue();
                    }
                }
                else{
                    if(!snapshot.child("user1").exists()){
                        reference.child(push_key).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}