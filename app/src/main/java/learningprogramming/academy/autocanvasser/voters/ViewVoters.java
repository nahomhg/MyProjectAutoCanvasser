package learningprogramming.academy.autocanvasser.voters;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;
import learningprogramming.academy.autocanvasser.homepage.AdminHome;
import learningprogramming.academy.autocanvasser.homepage.CanvasserHome;
import learningprogramming.academy.autocanvasser.voters.votersDialog.VoterDetailsDialog;
import learningprogramming.academy.autocanvasser.voters.votersDialog.VoterDialogListener;


public class ViewVoters extends AppCompatActivity implements VoterDialogListener, PopupMenu.OnMenuItemClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ListView listView;
    private Button settingBtn;
    private ProgressBar progressBar;

    private static final String TAG = "ViewVoters";
    private List<Voters> listOfVoters;
    public static final String CANVASSERS_TABLE = "canvassers";
    public static final String VOTERS_TABLE = "voters";
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_voters);
        listOfVoters = new ArrayList<>();

        listView = findViewById(R.id.voterListView);
        settingBtn = findViewById(R.id.settingsBtn);
        progressBar = findViewById(R.id.progressBarIcon);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserUid = firebaseAuth.getUid();

        getDataAboutCanvasserArea();

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Voters voters = (Voters) parent.getItemAtPosition(position);
                int electoralNumber = voters.getEN();
                openDialog(electoralNumber);
            }
        });
        listView.setDividerHeight(3);
    }

    public void getDataAboutCanvasserArea(){
        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(firebaseAuth.getCurrentUser().getUid())){
                            if(snapshot.getData().get("area").toString().equals("") || snapshot.getData().get("area").toString().isEmpty()){
                                displayAreaNotAssignedMessage();
                            }else
                                retrieveVoterData(VOTERS_TABLE, snapshot.getData().get("area").toString(),
                                        snapshot.getData().get("party").toString());
                        }
                    }
                }
            }
        });
    }

    private void displayAreaNotAssignedMessage(){
        new AlertDialog.Builder(ViewVoters.this).setTitle("Area Not Assigned!")
                .setMessage("Your campaign manager has not assigned an area for you. \n" +
                "Until you are assigned an area, you \ncannot start canvassing")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        overridePendingTransition(R.anim.fragment_close_enter, R.anim.fragment_fade_exit);
                        homePage();
                    }
                })
                .setIcon(R.drawable.ic_warning_yellow_28dp)
                .show();
    }

    private void openDialog(int electoralNumber){
        VoterDetailsDialog voterDetailsDialog = new VoterDetailsDialog(electoralNumber);
        voterDetailsDialog.show(getSupportFragmentManager(), null);
        if(voterDetailsDialog.getDialogStatus())
            refreshPage();
    }

    @Override
    public void applyVoterInformation(int electoralNumber, int electoralNumberSuffix, String electoralNumberPrefix,
                                 final String fullName, String address, String city, String postCode, String partyChosen,
                                 String[] questions, String documentReference, String canvasserWhoContactedVoter, String contactInformation) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("voters").document(documentReference);
        Log.d(TAG, "APPLY INFORMATION: ID RECEIVED: "+documentReference);
        Map<String, Object> updateDatabase = new HashMap<>();
        updateDatabase.put("partyChosen", partyChosen);
        if(questions.length > 0)
            updateDatabase.put("questions", Arrays.asList(questions));
        if(!contactInformation.isEmpty())
            updateDatabase.put("contactInformation", contactInformation);
        updateDatabase.put("contacted", true);
        updateDatabase.put("voterContactedBy", canvasserWhoContactedVoter);

        docRef.update(updateDatabase).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "UPDATE HAS BEEN SUCCESSFUL");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Update has FAILED!");
            }
        });

        insertIntoVotersContactedTable(electoralNumberPrefix, electoralNumber, electoralNumberSuffix,fullName,
                address, city, postCode, partyChosen, questions, contactInformation, canvasserWhoContactedVoter, documentReference);
    }

    @Override
    public void cancelDialog() {
        refreshPage();
    }


    private void insertIntoVotersContactedTable(String electoralNumberPrefix, int electoralNumber, int electoralNumberSuffix,
                                                final String fullName, String address, String city, String postCode,
                                                final String partyChosen, String[] questions, String contactInformation,
                                                String canvasserWhoContactedVoter, String documentReference){
        final Map<String, Object> insertUser = new HashMap<>();
        insertUser.put("enp", electoralNumberPrefix);
        insertUser.put("en", electoralNumber);
        insertUser.put("ens", electoralNumberSuffix);
        insertUser.put("firstName", fullName.split(" ")[0]);
        insertUser.put("lastName", fullName.split(" ")[1]);
        insertUser.put("address1", address.split(",")[0]);
        insertUser.put("address2", address.split(",")[1]);
        insertUser.put("city", city);
        insertUser.put("postalCode", postCode);
        insertUser.put("partyChosen", partyChosen);
        insertUser.put("questions", Arrays.asList(questions));
        insertUser.put("contactInfo", contactInformation);
        insertUser.put("voterContactedBy", canvasserWhoContactedVoter);
        insertUser.put("contacted", true);

        firebaseFirestore.collection("voterscontacted").document(documentReference)
                .set(insertUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> partyData = new HashMap<>();
                partyData.put("name", partyChosen);
                partyData.put("voterCount", FieldValue.increment(1));

                firebaseFirestore.collection("party").document(partyChosen)
                        .update(partyData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "party info added");
                        refreshPage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "ERROR: party info NOT added: "+e.getMessage());
                    }
;                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "ERROR: Could NOT add user to voters contacted table");
            }
        });
    }

    private ListView retrieveVoterData(final String collectionName, final String areaOfVoter, final String partyName){
        progressBar.setVisibility(View.VISIBLE);
        firebaseFirestore.collection(collectionName).orderBy("address1").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if(documentSnapshot.getData().get("enp").toString().equals(areaOfVoter) &&
                                !documentSnapshot.getData().get("partyChosen").toString().equals(partyName) &&
                                !documentSnapshot.getData().get("voterContactedBy").toString().equals(currentUserUid)){
                            Voters votersList = documentSnapshot.toObject(Voters.class);
                            listOfVoters.add(votersList);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    VotersAdapter votersAdapter = new VotersAdapter(ViewVoters.this, listOfVoters);
                    votersAdapter.notifyDataSetChasnged();
                    listView.setAdapter(votersAdapter);
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
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), UserLogin.class));
    }

    private void refreshPage(){
        finish();
        startActivity(new Intent(getApplicationContext(), ViewVoters.class));
    }
    @Override
    public void onBackPressed() {
        //do nothing
    }

    public void homePage(){
        firebaseFirestore.collection("canvassers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        if (currentUserUid.equals(snapshot.getData().get("authenticationID"))) {
                            if (!snapshot.getBoolean("admin"))
                                startActivity(new Intent(getApplicationContext(), CanvasserHome.class));
                             else
                                startActivity(new Intent(getApplicationContext(), AdminHome.class));
                        }
                    }
                }
            }
        });
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
            default:
                return false;
        }
    }

}