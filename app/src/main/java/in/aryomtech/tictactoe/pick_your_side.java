package in.aryomtech.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Random;

import soup.neumorphism.NeumorphButton;
import www.sanju.motiontoast.MotionToast;

public class pick_your_side extends AppCompatActivity {

    ImageView circle,cross;
    String checked,roomId;
    FirebaseAuth auth;
    FirebaseUser user;
    String device_token;
    DatabaseReference user_reference;
    EditText editText_name;
    DatabaseReference reference;
    NeumorphButton continue_button;
    RadioButton radioButton_circle,radioButton_cross;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_your_side);

        Window window = pick_your_side.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(pick_your_side.this, R.color.white));

        roomId=getIntent().getStringExtra("key_for_room");
        reference= FirebaseDatabase.getInstance().getReference().child("roomID");
        user_reference=FirebaseDatabase.getInstance().getReference().child("users");
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        circle=findViewById(R.id.imageView8);
        radioButton_circle=findViewById(R.id.radioButton2);
        radioButton_cross=findViewById(R.id.radioButton);
        cross=findViewById(R.id.imageView7);
        continue_button=findViewById(R.id.continue_but);

        editText_name=findViewById(R.id.editTextPhone2);
        user_reference.child(user.getUid()+"").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists())
                    editText_name.setText(snapshot.child("name").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        circle.setOnClickListener(v->{
            radioButton_circle.setChecked(!radioButton_circle.isChecked());
            if(radioButton_circle.isChecked())
                radioButton_cross.setChecked(false);
        });
        cross.setOnClickListener(v->{
            radioButton_cross.setChecked(!radioButton_cross.isChecked());
            if(radioButton_cross.isChecked())
                radioButton_circle.setChecked(false);
        });
        if(user==null){
            get_device_token();
            auth.signInAnonymously().addOnCompleteListener(task -> {

                MotionToast.Companion.darkColorToast(pick_your_side.this,
                        "Welcome \uD83D\uDE03",
                        "Signed in - Anonymously.",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(pick_your_side.this, R.font.helvetica_regular));

                user=auth.getCurrentUser();
                user_reference.child(user.getUid()).child("uid").setValue(user.getUid());
                user_reference.child(user.getUid()).child("token").setValue(device_token);

            });
        }
        continue_button.setOnClickListener(v->{
            if(!(radioButton_circle.isChecked() && radioButton_cross.isChecked())){
                if (radioButton_circle.isChecked()) {
                    checked = "o";
                    reference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child("user1").exists() && snapshot.child("user2").exists()){
                                MotionToast.Companion.darkColorToast(pick_your_side.this,
                                        "Room is full",
                                        "join or create another room.",
                                        MotionToast.TOAST_WARNING,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(pick_your_side.this,R.font.helvetica_regular));
                            }
                            else{
                                if (!snapshot.child("user1").exists()) {
                                    save_name();
                                    reference.child(roomId).child("user1").setValue(user.getUid());
                                    Intent intent = new Intent(pick_your_side.this, MainActivity.class);
                                    intent.putExtra("key_for_roomID", roomId);
                                    intent.putExtra("which_user_?", "user1");
                                    intent.putExtra("picked_choice", checked);
                                    intent.putExtra("name_of_user", editText_name.getText().toString());
                                    startActivity(intent);
                                }
                                else {
                                    save_name();
                                    reference.child(roomId).child("user2").setValue(user.getUid());
                                    Intent intent = new Intent(pick_your_side.this, MainActivity.class);
                                    intent.putExtra("key_for_roomID", roomId);
                                    intent.putExtra("which_user_?", "user2");
                                    intent.putExtra("picked_choice", checked);
                                    intent.putExtra("name_of_user", editText_name.getText().toString());
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else if (radioButton_cross.isChecked()) {
                    checked = "x";
                    reference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child("user1").exists() && snapshot.child("user2").exists()) {
                                MotionToast.Companion.darkColorToast(pick_your_side.this,
                                        "Room is full",
                                        "join another room.",
                                        MotionToast.TOAST_WARNING,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(pick_your_side.this, R.font.helvetica_regular));
                            } else {
                                if (!snapshot.child("user1").exists()) {
                                    save_name();
                                    reference.child(roomId).child("user1").setValue(user.getUid());
                                    Intent intent = new Intent(pick_your_side.this, MainActivity.class);
                                    intent.putExtra("key_for_roomID", roomId);
                                    intent.putExtra("which_user_?", "user1");
                                    intent.putExtra("picked_choice", checked);
                                    intent.putExtra("name_of_user", editText_name.getText().toString());
                                    startActivity(intent);
                                } else {
                                    save_name();
                                    reference.child(roomId).child("user2").setValue(user.getUid());
                                    Intent intent = new Intent(pick_your_side.this, MainActivity.class);
                                    intent.putExtra("key_for_roomID", roomId);
                                    intent.putExtra("which_user_?", "user2");
                                    intent.putExtra("picked_choice", checked);
                                    intent.putExtra("name_of_user", editText_name.getText().toString());
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
        });

    }

    private void save_name() {
        if(editText_name.getText().toString().equals("")){
            Random random=new Random();
            int rand=random.nextInt(1000000000);
            user_reference.child(user.getUid()).child("name").setValue("guest"+rand);
        }
        else{
            user_reference.child(user.getUid()).child("name").setValue(editText_name.getText().toString());
        }
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
}