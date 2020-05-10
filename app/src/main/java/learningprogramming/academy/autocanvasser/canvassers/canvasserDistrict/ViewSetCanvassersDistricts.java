package learningprogramming.academy.autocanvasser.canvassers.canvasserDistrict;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;
import learningprogramming.academy.autocanvasser.canvassers.CanvasserAdapter;
import learningprogramming.academy.autocanvasser.canvassers.Canvassers;
import learningprogramming.academy.autocanvasser.homepage.AdminHome;

public class ViewSetCanvassersDistricts extends AppCompatActivity implements SetDistrictInterfaceListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "ViewSetDistrictActivity";
    private static final String CANVASSERS_COLLECTION = "canvassers";
    private List<Canvassers> listOfCanvassers;
    public static String politicalParty;
    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference docRef;

    private ListView listView;
    private Button settingBtn;
    private TextView campaignManagersParty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_set_canvassers_district);

        listOfCanvassers = new ArrayList<>();

        settingBtn = findViewById(R.id.settingsBtnCanvassers);
        listView = findViewById(R.id.canvasserContactedListView);
        campaignManagersParty = findViewById(R.id.leadersParty);

        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });


        firebaseFirestore.collection(CANVASSERS_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(userAuth.getUid())){
                            campaignManagersParty.setText(snapshot.getData().get("party").toString());
                            retrieveCanvassersInPartyData(CANVASSERS_COLLECTION, campaignManagersParty.getText().toString());
                        }
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Canvassers canvasserChosen = (Canvassers) parent.getItemAtPosition(position);
                String canvasserChosenAuthenticationID = canvasserChosen.getAuthenticationId();
                openDialog(canvasserChosenAuthenticationID);
            }
        });
        listView.setDividerHeight(3);
    }

    private ListView retrieveCanvassersInPartyData(String documentName, final String party){
        firebaseFirestore.collection(documentName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("party").equals(party)
                                && !snapshot.getData().get("authenticationID").equals(userAuth.getCurrentUser().getUid())
                                && snapshot.getBoolean("approved")) {
                            Canvassers canvasserList = snapshot.toObject(Canvassers.class);
                            listOfCanvassers.add(canvasserList);
                            Log.d(TAG,"list: "+listOfCanvassers);
                        }
                    }
                    CanvasserAdapter canvasserAdapter = new CanvasserAdapter(ViewSetCanvassersDistricts.this, listOfCanvassers);
                    canvasserAdapter.notifyDataSetChanged();
                    listView.setAdapter(canvasserAdapter);
                }
            }
        });
        return listView;
    }
    private void openDialog(String electoralNumber){
        SetCanvassersDistrictDialog setCanvassersDistrictDialog = new SetCanvassersDistrictDialog(electoralNumber);
        setCanvassersDistrictDialog.show(getSupportFragmentManager(), null);
        //Sending the id of the user (canvasser) to the SetCanvasserDistrictDialog class, from there it will find the user with that id.
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
        startActivity(new Intent(getApplicationContext(), ViewSetCanvassersDistricts.class));
    }
    @Override
    public void onBackPressed() {
        //do nothing
    }

    public void homePage(){
        startActivity(new Intent(getApplicationContext(), AdminHome.class));
    }

    public void accountInfo(){
        Toast.makeText(ViewSetCanvassersDistricts.this, "User: "+userAuth.getUid()+"\nEmail: "+userAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
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
    public void applyInformation(String id, String fullName, String emailAddress, String district, String areaOfDistrict, String documentRef) {
        Log.d(TAG, "DISTRICT VIEW INFORMATION: test ");

        DocumentReference referenceForCanvassers = FirebaseFirestore.getInstance().collection(CANVASSERS_COLLECTION).document(documentRef);
        Log.d(TAG, "DISTRICT VIEW INFORMATION: ID RECEIVED: "+documentRef);
        Map<String, Object> updateCanvassers = new HashMap<>();
        updateCanvassers.put("district", district);
        updateCanvassers.put("area", areaOfDistrict);

        referenceForCanvassers.update(updateCanvassers).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "UPDATE HAS BEEN SUCCESSFUL");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "!!! ERROR: UPDATE HAS NOT BEEN SUCCESSFUL");
            }
        });
        }

    @Override
    public void cancelDialog() {
        refreshPage();
    }


}
