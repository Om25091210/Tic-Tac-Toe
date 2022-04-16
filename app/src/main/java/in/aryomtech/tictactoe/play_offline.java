package in.aryomtech.tictactoe;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class play_offline extends AppCompatActivity {

    boolean gameActive = true;
    Dialog dialog;
    private AdView mAdView;
    Animation animation1;
    TextView x_point,o_point;
    // Player representation
    // 0 - X
    // 1 - O
    int activePlayer = 0;
    int count=0;
    List<Integer> x_taps=new ArrayList<>();
    List<Integer> o_taps=new ArrayList<>();
    List<String> win_positions_str=new ArrayList<>();
    List<String> x_positions=new ArrayList<>();
    List<String> o_positions=new ArrayList<>();
    ImageView img1,img2,img3,img4,img5,img6,img7,img8,img9;
    int count_x=0,count_o=0;
    int[] gameState = {2, 2 , 2, 2, 2, 2, 2, 2, 2};
    //    State meanings:
    //    0 - X
    //    1 - O
    //    2 - Null
    MediaPlayer draw_sound,win_sound,ring_for_user,ring_for_opponent;
    int[][] winPositions = {{0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}};
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_offline);

        Window window = play_offline.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(play_offline.this, R.color.white));

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
            public void onAdFailedToLoad(LoadAdError adError) {
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

        win_sound= MediaPlayer.create(play_offline.this,R.raw.win_sound);
        draw_sound= MediaPlayer.create(play_offline.this,R.raw.draw_sound);

        ring_for_opponent= MediaPlayer.create(play_offline.this,R.raw.user_click_sound);
        ring_for_user= MediaPlayer.create(play_offline.this,R.raw.opponent_click_sound);

        animation1 = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bounce);
        x_point=findViewById(R.id.textView);
        o_point=findViewById(R.id.textView3);

        win_positions_str.add("[0, 1, 2]");
        win_positions_str.add("[0, 3, 6]");
        win_positions_str.add("[0, 4, 8]");
        win_positions_str.add("[3, 4, 5]");
        win_positions_str.add("[1, 4, 7]");
        win_positions_str.add("[2, 4, 6]");
        win_positions_str.add("[6, 7, 8]");
        win_positions_str.add("[2, 5, 8]");

        img1=findViewById(R.id.img1);
        img2=findViewById(R.id.img2);
        img3=findViewById(R.id.img3);
        img4=findViewById(R.id.img4);
        img5=findViewById(R.id.img5);
        img6=findViewById(R.id.img6);
        img7=findViewById(R.id.img7);
        img8=findViewById(R.id.img8);
        img9=findViewById(R.id.img9);

    }

    public void playerTap(View view) {
        ImageView img = (ImageView) view;
        int tappedImage = Integer.parseInt(img.getTag().toString());
        if (!gameActive) {
            gameReset();
        }
        if (gameState[tappedImage] == 2) {
            gameState[tappedImage] = activePlayer;
            if (activePlayer == 0) {
                ring_for_user.start();
                img.startAnimation(animation1);
                img.setImageResource(R.drawable.ic_cross);
                x_taps.add(tappedImage);
                draw_line(x_taps,"x");
                activePlayer = 1;
                TextView status = findViewById(R.id.textView9);
                String o_text="O's Turn";
                status.setText(o_text);
                if(count==8 && check_win_status()==0)
                    show_draw();
                count++;
                new Handler(Looper.myLooper()).postDelayed(() -> img.setAnimation(null),500);
            } else {
                ring_for_opponent.start();
                img.startAnimation(animation1);
                img.setImageResource(R.drawable.ic_circle);
                o_taps.add(tappedImage);
                draw_line(o_taps,"o");
                activePlayer = 0;
                TextView status = findViewById(R.id.textView9);
                String x_turn="X's Turn";
                status.setText(x_turn);
                if(count==8 && check_win_status()==0)
                    show_draw();
                count++;
                new Handler(Looper.myLooper()).postDelayed(() -> img.setAnimation(null),500);
            }
        }
        // Check if any player has won
        for (int[] winPosition : winPositions) {
            if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
                    gameState[winPosition[1]] == gameState[winPosition[2]] &&
                    gameState[winPosition[0]] != 2) {
                // Somebody has won! - Find out who!
                gameActive = false;
                if (gameState[winPosition[0]] == 0) {
                    show_win("X");
                    count_x++;
                    String text_x=count_x+"";
                    x_point.setText(text_x);
                    break;
                } else {
                    show_win("O");
                    count_o++;
                    String text_o=count_o+"";
                    o_point.setText(text_o);
                    break;
                }
            }
        }
    }

    private void draw_line(List<Integer> player_taps,String sign) {
        for(int i:player_taps){
            for (int j: player_taps){
                if(j==i)
                    continue;
                for(int k:player_taps){
                    if(k==i || k==j)
                        continue;
                    if(sign.equals("x")) {
                        x_positions.add("[" + i + ", " + j + ", " + k + "]");
                    }
                    else
                        o_positions.add("["+i+", "+j+", "+k+"]");
                }
            }
        }
        for(int a=0;a<win_positions_str.size();a++){
            if(sign.equals("x")){
                Log.e("the positions are ",win_positions_str.get(a));
                if(x_positions.contains(win_positions_str.get(a))){
                    Log.e("the positions are ",win_positions_str.get(a));
                    set_line(win_positions_str.get(a));
                    break;
                }
            }
            else{
                if(o_positions.contains(win_positions_str.get(a))){
                    set_line(win_positions_str.get(a));
                    break;
                }
            }
        }
    }
    private void set_line(String positions) {
        if(positions.equals(win_positions_str.get(0))){
            img1.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img2.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img3.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(win_positions_str.get(1))){
            img1.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img4.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img7.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
        else if(positions.equals(win_positions_str.get(2))){
            img1.setBackgroundResource(R.drawable.ic_left_dialog);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_left_dialog),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_left_dialog),400);
        }
        else if(positions.equals(win_positions_str.get(3))){
            img4.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img6.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(win_positions_str.get(4))){
            img2.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img8.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
        else if(positions.equals(win_positions_str.get(5))){
            img3.setBackgroundResource(R.drawable.ic_right_diagonal);
            new Handler(Looper.myLooper()).postDelayed(() -> img5.setBackgroundResource(R.drawable.ic_right_diagonal),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img7.setBackgroundResource(R.drawable.ic_right_diagonal),400);
        }
        else if(positions.equals(win_positions_str.get(6))){
            img7.setBackgroundResource(R.drawable.ic_mid_straight);
            new Handler(Looper.myLooper()).postDelayed(() -> img8.setBackgroundResource(R.drawable.ic_mid_straight),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_mid_straight),400);
        }
        else if(positions.equals(win_positions_str.get(7))){
            img3.setBackgroundResource(R.drawable.ic_mid_top);
            new Handler(Looper.myLooper()).postDelayed(() -> img6.setBackgroundResource(R.drawable.ic_mid_top),200);
            new Handler(Looper.myLooper()).postDelayed(() -> img9.setBackgroundResource(R.drawable.ic_mid_top),400);
        }
    }

    private void show_win(String sign) {
        dialog = new Dialog(play_offline.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_lose_dialog);
        LottieAnimationView lottieAnimationView=dialog.findViewById(R.id.trophy);
        LottieAnimationView lottieAnimationView1=dialog.findViewById(R.id.animation_view);
        lottieAnimationView1.setVisibility(View.GONE);
        lottieAnimationView.setAnimation("offline_win.json");
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        TextView msg=dialog.findViewById(R.id.textView2);
        String text=sign+" has won";
        msg.setText(text);
        TextView play_again=dialog.findViewById(R.id.msg);
        play_again.setOnClickListener(v->{
                gameReset();
                dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!play_offline.this.isFinishing() && !dialog.isShowing()) {
            win_sound.start();
            dialog.show();
        }
    }

    private void show_draw() {
        dialog = new Dialog(play_offline.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.win_lose_dialog);
        LottieAnimationView lottieAnimationView=dialog.findViewById(R.id.trophy);
        LottieAnimationView lottieAnimationView1=dialog.findViewById(R.id.animation_view);
        lottieAnimationView1.setVisibility(View.GONE);
        lottieAnimationView.setAnimation("draw_offline.json");
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
        TextView msg=dialog.findViewById(R.id.textView2);
        String text="Match tie";
        msg.setText(text);
        TextView play_again=dialog.findViewById(R.id.msg);
        play_again.setOnClickListener(v->{
            gameReset();
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!play_offline.this.isFinishing() && !dialog.isShowing()) {
            draw_sound.start();
            dialog.show();
        }
    }

    public void gameReset() {
        gameActive = true;
        count=0;
        activePlayer = 0;
        for(int i=0; i<gameState.length; i++){
            gameState[i] = 2;
        }

        x_positions.clear();
        o_positions.clear();
        o_taps.clear();
        x_taps.clear();

        ((ImageView)findViewById(R.id.img1)).setImageResource(0);
        ((ImageView)findViewById(R.id.img2)).setImageResource(0);
        ((ImageView)findViewById(R.id.img3)).setImageResource(0);
        ((ImageView)findViewById(R.id.img4)).setImageResource(0);
        ((ImageView)findViewById(R.id.img5)).setImageResource(0);
        ((ImageView)findViewById(R.id.img6)).setImageResource(0);
        ((ImageView)findViewById(R.id.img7)).setImageResource(0);
        ((ImageView)findViewById(R.id.img8)).setImageResource(0);
        ((ImageView)findViewById(R.id.img9)).setImageResource(0);

        img1.setBackgroundResource(android.R.color.transparent);
        img2.setBackgroundResource(android.R.color.transparent);
        img3.setBackgroundResource(android.R.color.transparent);
        img4.setBackgroundResource(android.R.color.transparent);
        img5.setBackgroundResource(android.R.color.transparent);
        img6.setBackgroundResource(android.R.color.transparent);
        img7.setBackgroundResource(android.R.color.transparent);
        img8.setBackgroundResource(android.R.color.transparent);
        img9.setBackgroundResource(android.R.color.transparent);

        TextView status = findViewById(R.id.textView9);
        String tx="X's Turn";
        status.setText(tx);

    }

    private int check_win_status(){
        int i=0;
        // Check if any player has won
        for (int[] winPosition : winPositions) {
            if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
                    gameState[winPosition[1]] == gameState[winPosition[2]] &&
                    gameState[winPosition[0]] != 2) {
                // Somebody has won! - Find out who!
                gameActive = false;
                if (gameState[winPosition[0]] == 0) {
                    i=1;
                } else {
                    i=1;
                }
            }
        }
        return i;
    }

}