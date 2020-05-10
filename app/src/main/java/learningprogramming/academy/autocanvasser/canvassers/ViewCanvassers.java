package learningprogramming.academy.autocanvasser.canvassers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.canvassers.dialog.popup.CanvasserDialogListener;
import learningprogramming.academy.autocanvasser.canvassers.dialog.popup.CanvassersApprovalDialog;
import learningprogramming.academy.autocanvasser.homepage.AdminHome;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;


public class ViewCanvassers extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, CanvasserDialogListener{

    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference docRef;
    public static final String CANVASSERS_TABLE = "canvassers";

    private ListView listView;
    private Button settingBtn;

    private ProgressBar progressBarIcon;
    private static final String TAG = "ViewCanvassers: ";
    private List<Canvassers> listOfCanvassers;
    private static int id = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_canvassers);
        listOfCanvassers = new ArrayList<>();

        listView = findViewById(R.id.canvasserContactedListView);
        settingBtn = findViewById(R.id.settingsBtnCanvassers);
        progressBarIcon = findViewById(R.id.progressBar);

        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();

        retrieveData(CANVASSERS_TABLE);

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView lv = (ListView) parent;

                Log.d(TAG, " LOGGER: --- position: "+position+" -- "+parent.getItemAtPosition(position).toString());
                Canvassers canvasserChosen = (Canvassers) parent.getItemAtPosition(position);
                String canvasserChosenAuthenticationID = canvasserChosen.getAuthenticationId();
                openDialog(canvasserChosenAuthenticationID);
            }
        });
        listView.setDividerHeight(5);
    }

    private void openDialog(String canvasserAuthenticationID){
        CanvassersApprovalDialog canvassersApprovalDialog = new CanvassersApprovalDialog(canvasserAuthenticationID);
        canvassersApprovalDialog.show(getSupportFragmentManager(), null);
    }

    private ListView retrieveData(String documentName){
        firebaseFirestore.collection(documentName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()) {
                        Canvassers canvasserList = snapshot.toObject(Canvassers.class);
                        if (!snapshot.getBoolean("approved")) {
                            listOfCanvassers.add(canvasserList);
                            //Log.d(TAG, "USER CANVASSER = " + snapshot.getId() + " - " + snapshot.getData().get("firstName") + " - email: " + snapshot.getData().get("email") + " id: " + snapshot.getData().get("id"));
                            progressBarIcon.setVisibility(View.INVISIBLE);
                        }

                    }
                    CanvasserAdapter canvasserAdapter = new CanvasserAdapter(ViewCanvassers.this, listOfCanvassers);
                    canvasserAdapter.notifyDataSetChanged();
                    listView.setAdapter(canvasserAdapter);
                }
            }
        });
        return listView;
    }


    public void settings(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu);
        popup.show();
    }

    private void signOut() {
        userAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), UserLogin.class));
    }

    private void refreshPage(){
        finish();
        startActivity(new Intent(getApplicationContext(), ViewCanvassers.class));
    }
    @Override
    public void onBackPressed() {
        //do nothing
    }

    public void homePage(){
        startActivity(new Intent(getApplicationContext(), AdminHome.class));
    }

    public void accountInfo(){
        Toast.makeText(ViewCanvassers.this, "User: "+userAuth.getUid()+"\nEmail: "+userAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutBtn:
                signOut();
                return true;
            case R.id.refreshBtn:
                refreshPage();
                return true;
            case R.id.homeBtn:
                homePage();
                return true;
            case R.id.accountBtn:
                accountInfo();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void approveUser(final int id, final String fullName, final String emailAddress
            , final String politicalParty, String documentReference) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(CANVASSERS_TABLE).document(documentReference);
        Log.d(TAG, "APPLY INFORMATION: ID RECEIVED: "+documentReference);
        Map<String, Object> updateCanvassersApprovalField = new HashMap<>();
        updateCanvassersApprovalField.put("approved", true);

        docRef.update(updateCanvassersApprovalField).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "UPDATE HAS BEEN SUCCESSFUL");
                Toast.makeText(ViewCanvassers.this,
                        "Please Go to 'Assign District' and assign the user a district and area", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "ERROR: UPDATE HAS BEEN UNSUCCESSFUL");
            }
        });

        Log.d(TAG, "Current user AFTER 2: "+userAuth.getCurrentUser().getEmail());
    }

    @Override
    public void rejectUser(int id, String fullName, String emailAddress, String documentReference) {
        firebaseFirestore.collection(CANVASSERS_TABLE).document(documentReference).delete();
        refreshPage();
    }

    @Override
    public void cancelDialog() {
        refreshPage();
    }

    private void resetUserPassword(final String userEmailAddress){
        userAuth.sendPasswordResetEmail(userEmailAddress).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "SUCCESS USER CANVASSER = "+userEmailAddress+" - done");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "FAILURE USER CANVASSER = "+userEmailAddress+" - WAS NOT done");
            }
        });
        Log.d(TAG, "Current user AFTER: "+userAuth.getCurrentUser().getEmail());
    }

    public String randomPassword(){
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

}