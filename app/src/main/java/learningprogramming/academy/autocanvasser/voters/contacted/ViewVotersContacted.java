package learningprogramming.academy.autocanvasser.voters.contacted;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;
import learningprogramming.academy.autocanvasser.homepage.AdminHome;
import learningprogramming.academy.autocanvasser.homepage.CanvasserHome;
import learningprogramming.academy.autocanvasser.voters.Voters;
import learningprogramming.academy.autocanvasser.voters.VotersAdapter;
import learningprogramming.academy.autocanvasser.voters.votersDialog.VoterDialogListener;

public class ViewVotersContacted extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, VoterDialogListener {

    private Button settingsBtn;
    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;

    public static final String TAG = "VotersContacted";
    private static final String CANVASSERS_TABLE = "canvassers";
    public static final String VOTERS_TABLE = "voters";
    private static final String VOTERS_CONTACTED_TABLE = "voterscontacted";

    private String currentUserID;
    private List<Voters> listOfVoters;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voters_contacted);

        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        currentUserID = userAuth.getUid();
        listOfVoters = new ArrayList<>();

        listView = findViewById(R.id.canvasserContactedListView);
        settingsBtn = findViewById(R.id.settingsBtnCanvassers);

        getDataAboutCanvasserParty();

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Voters voters = (Voters) parent.getItemAtPosition(position);
                int voterContactedElectoralNumber = voters.getEN();
                openDialog(voterContactedElectoralNumber);
            }
        });

        listView.setDividerHeight(3);
    }

    private void openDialog(int electoralNumber){
        VotersContactedDialog votersContactedDialog = new VotersContactedDialog(electoralNumber);
        votersContactedDialog.show(getSupportFragmentManager(), null);
    }

    private void getDataAboutCanvasserParty(){
        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(currentUserID)){
                            retrieveVotersContactedData(VOTERS_CONTACTED_TABLE, snapshot.getData().get("party").toString());
                        }
                    }
                }
            }
        });
    }
    private ListView retrieveVotersContactedData(final String documentName, final String partyOfCanvasser){
        firebaseFirestore.collection(documentName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        if(documentSnapshot.getData().get("partyChosen").toString().equals(partyOfCanvasser)){
                            Voters votersList = documentSnapshot.toObject(Voters.class);
                            listOfVoters.add(votersList);
                        }
                    }
                }
                VotersAdapter votersAdapter = new VotersAdapter(ViewVotersContacted.this, listOfVoters);
                votersAdapter.notifyDataSetChanged();
                listView.setAdapter(votersAdapter);
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
        startActivity(new Intent(getApplicationContext(), ViewVotersContacted.class));
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
                        if (currentUserID.equals(snapshot.getData().get("authenticationID"))) {
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


    @Override
    public void applyVoterInformation(int electoralNumber, int electoralNumberSuffix, String electoralNumberPrefix, String fullName, String address, String city, String postCode, String partyChoice, String[] voterQuestions, String documentReference, String contactedByCanvasserName, String contactIno) {
        //Do Nothing
    }

    @Override
    public void cancelDialog() {
        refreshPage();
    }
}
