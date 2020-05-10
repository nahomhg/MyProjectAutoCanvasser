package learningprogramming.academy.autocanvasser.userLoginRegister.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.userLoginRegister.forgotPassword.ForgotPassword;
import learningprogramming.academy.autocanvasser.userLoginRegister.register.RegisterCanvasser;
import learningprogramming.academy.autocanvasser.userLoginRegister.register.RegisterUserDialogListener;
import learningprogramming.academy.autocanvasser.canvassers.Canvassers;
import learningprogramming.academy.autocanvasser.homepage.AdminHome;
import learningprogramming.academy.autocanvasser.homepage.CanvasserHome;


public class UserLogin extends AppCompatActivity implements RegisterUserDialogListener {

    private static final String CANVASSERS_TABLE = "canvassers";
    private Button registerbtn;
    private Button loginBtn;
    private Button forgotPassword;
    private EditText email;
    private EditText password;

    private int userId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Canvassers canvasserObject;
    private String partyChosen;
    private static final String TAG = "UserLoginActivtiy";

    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private CollectionReference canvassersTableReference = firebaseFirestore.collection(CANVASSERS_TABLE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        userAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.passwordResetEmail);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerbtn = findViewById(R.id.registerBtn);
        forgotPassword = findViewById(R.id.resetPassword);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = email.getText().toString();
                String loginPassword = password.getText().toString();
                if (loginValidator(loginEmail, loginPassword)){
                    signIn(loginEmail, loginPassword);
                }
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ForgotPassword.class));
                overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_pop_exit_anim);
            }
        });
    }

    private void openDialog() {
        RegisterCanvasser registerUser = new RegisterCanvasser();
        registerUser.show(getSupportFragmentManager(), null);
    }

    @Override
    public void createUserAccount(final int id, final String fname, final String lname, final String email, final String party) {
        this.userId = id;
        this.firstName = fname;
        this.lastName = lname;
        this.emailAddress = email;
        this.partyChosen = party;

        if(!emailAddress.isEmpty()) {
            //Create an account
            userAuth.createUserWithEmailAndPassword(emailAddress, randomPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        List<Object> authenId = new ArrayList<>();
                        final FirebaseUser user = userAuth.getCurrentUser();
                        //Log.d(TAG, "SUCCESS: User " + user.getEmail() + " with id: " + user.getUid() + " has been created");
                        authenId.add(user.getUid());
                        insertUserInformationToDatabase(authenId, userId, firstName, lastName, emailAddress, partyChosen);
                        Log.d(TAG, "TESTING EMAIL ADDRESS: " + emailAddress);
                        sendResetPasswordEmail(emailAddress);
                    } else {
                        Log.d(TAG, "ERROR: User could NOT be created");

                    }
                }
            });
        }
        else if(emailAddress.isEmpty()){
            Toast.makeText(UserLogin.this, "ERROR: Could not register email, account may already exist with that name", Toast.LENGTH_LONG).show();
        }
   }

    private <E> void insertUserInformationToDatabase(List<E> authenticationID, int userId, String firstName, String lastName,
                                                     String emailAddress, String partyChosen ){
        canvasserObject = new Canvassers(authenticationID.get(0).toString(),firstName, lastName, emailAddress, partyChosen);

        Log.d(TAG, "Authenticaiton IDs: "+authenticationID.get(0).toString());
        final String userAuthenticationID = authenticationID.get(0).toString();
        Map<String, Object> canvassers = new HashMap<>();

        //Set the values on the information bored to the values on the Canvasser object.
        canvasserObject.setFirstName(firstName);
        canvasserObject.setLastName(lastName);
        canvasserObject.setEmail(emailAddress);
        canvasserObject.setParty(partyChosen);
        canvasserObject.setAuthenticationId(userAuthenticationID);

        canvassers.put("firstName", canvasserObject.getFirstName());
        canvassers.put("lastName", canvasserObject.getLastName());
        canvassers.put("email", canvasserObject.getEmail());
        canvassers.put("party", canvasserObject.getParty());
        canvassers.put("admin", false);
        canvassers.put("approved", false);
        canvassers.put("authenticationID", canvasserObject.getAuthenticationId());
        canvassers.put("district","");
        canvassers.put("area","");

        canvassersTableReference.add(canvassers).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful())
                    Toast.makeText(UserLogin.this, "Sign Up successful, now you will have to wait for approval", Toast.LENGTH_SHORT).show();
                if(!task.isSuccessful()){
                    Log.d(TAG, "ERROR: User could NOT be added to the table Canvassers");
                }
            }
        });

    }

    public boolean loginValidator(String loginEmail, String loginPassword) {
        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; //Code from TutorialPoint;
        String regex = "^(.+)@(.+)$";

        if (loginEmail.isEmpty() && loginPassword.isEmpty()) {
            email.setError("Please Fill In Details");
            password.setError("Please Fill In Details");
            return false;
        }
        else if(loginEmail.isEmpty())
                email.setError("Please Fill In Details");
        else if(loginPassword.isEmpty())
                password.setError("Please Fill In Details");
        else {
            if (loginEmail.trim().matches(emailRegex) || loginEmail.trim().matches(regex))
                return true;
            else {
                Toast.makeText(UserLogin.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    private void signIn(String loginEmail, String loginPassword) {
        userAuth.signInWithEmailAndPassword(loginEmail, loginPassword).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    showAppropriateUI(userAuth.getUid());
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(UserLogin.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(UserLogin.this, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(TAG, "ERROR: Something's gone wrong" + e.getMessage());
                    }
                    Toast.makeText(UserLogin.this, "ERROR: Login Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure: Unable to sign in! - "+e.getMessage());
            }
        });
    }

    private void showAppropriateUI(final String user) {
        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        if (user.equals(snapshot.getData().get("authenticationID"))) {
                            if(!snapshot.getBoolean("approved")){
                                Log.d(TAG, "ERROR: User ("+user+") haven't been approved yet");
                                Toast.makeText(UserLogin.this, "ERROR: You haven't been approved yet",
                                        Toast.LENGTH_LONG).show();
                            }
                            else if (snapshot.getBoolean("admin")) {
                                startActivity(new Intent(getApplicationContext(), AdminHome.class));
                                overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_pop_exit_anim);
                            } else if (!snapshot.getBoolean("admin")) {
                                startActivity(new Intent(getApplicationContext(), CanvasserHome.class));
                                overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_pop_exit_anim);
                            }
                        }
                    }
                }
                if (!task.isSuccessful()) {

                }
            }
        });
    }

    private String randomPassword(){
        String sampleSpace = "ABCDEFGHIBJKLMNOPQRSTUVWXYZ1234567890*!#@$%(";
        Random rand = new Random();
        char[] chars = new char[8];
        for(int i = 0; i < chars.length; i++){
            chars[i] = sampleSpace.charAt(rand.nextInt(sampleSpace.length()));
        }
        String generatedPassword = "";
        for(int j = 0; j < chars.length; j++){
            generatedPassword+=chars[j];
        }
        return generatedPassword;
    }
    private void sendResetPasswordEmail(final String userEmailAddress){
        userAuth.sendPasswordResetEmail(userEmailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "SUCCESS: Reset password email sent");
                } else if(!task.isSuccessful()){
                    Log.d(TAG, "ERROR: Reset password email NOT sent");
                }
            }
        });
    }
}
