package learningprogramming.academy.autocanvasser.userLoginRegister.forgotPassword;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailAddress;
    private Button submitEmailResetPassword;
    private ImageView sentEmailIcon;
    private static final String TAG = "Forgot Password";

    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();

        emailAddress = findViewById(R.id.passwordResetEmail);
        submitEmailResetPassword = findViewById(R.id.submit_area);
        sentEmailIcon = findViewById(R.id.emailSentIcon);

        //Make submit button uncheckable until user inputs their email
        submitEmailResetPassword.setEnabled(false);

        //Hide Email Sent icon until it is sent.
        sentEmailIcon.setVisibility(View.INVISIBLE);
        emailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "before text changed");
                submitEmailResetPassword.setClickable(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitEmailResetPassword.setEnabled(true);
                submitEmailResetPassword.setClickable(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text now changed");
            }
        });


        submitEmailResetPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userAuth.sendPasswordResetEmail(emailAddress.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "SUCCESS: Reset password email sent");
                            } else if(!task.isSuccessful()){
                                Log.d(TAG, "ERROR: Reset password email NOT sent");
                            }
                        }
                    });
                emailAddress.setVisibility(View.INVISIBLE);
                sentEmailIcon.setVisibility(View.VISIBLE);
                submitEmailResetPassword.setEnabled(false);
                submitEmailResetPassword.setClickable(false);
                }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
    }
}
