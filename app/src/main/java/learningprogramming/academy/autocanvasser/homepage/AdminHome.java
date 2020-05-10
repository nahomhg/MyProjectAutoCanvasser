package learningprogramming.academy.autocanvasser.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.canvassers.ViewCanvassers;
import learningprogramming.academy.autocanvasser.canvassers.canvasserDistrict.ViewSetCanvassersDistricts;
import learningprogramming.academy.autocanvasser.partyStats.PartyStatistics;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;
import learningprogramming.academy.autocanvasser.voters.ViewVoters;
import learningprogramming.academy.autocanvasser.voters.Voters;
import learningprogramming.academy.autocanvasser.voters.contacted.ViewVotersContacted;

public class AdminHome extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, HomeInformation {

    private Button startCanvassing, settingsBtn, votersContacted,
            canvassersToApprove, assignDistrict, profileBtn, partyStats;
    private TextView greetingUser;

    private String firstName;


    private static final String TAG = "AdminHomeActivity";
    public static final String CANVASSERS_TABLE = "canvassers";

    private List<Voters> list = new ArrayList<>();

    private FirebaseAuth userAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference votersData = database.collection("voters");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userAuth = FirebaseAuth.getInstance();
        currentUser = userAuth.getCurrentUser();
        setContentView(R.layout.activity_admin_home);

        startCanvassing = findViewById(R.id.startCanvassingBtn);
        settingsBtn =  findViewById(R.id.settingsBtn);
        votersContacted = findViewById(R.id.votersContactedBtn);
        canvassersToApprove = findViewById(R.id.addCanvasserBtn);
        greetingUser = findViewById(R.id.greetingUser);
        assignDistrict = findViewById(R.id.myDistrictBtn);
        profileBtn = findViewById(R.id.profileBtn);
        partyStats = findViewById(R.id.partyStatsBtn);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminHome.this, "Settings clicked...", Toast.LENGTH_SHORT).show();
            }
        });

        startCanvassing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewVoters.class));
                Toast.makeText(AdminHome.this, "Starting Canvassing...", Toast.LENGTH_SHORT).show();
            }
        });

        assignDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewSetCanvassersDistricts.class));
            }
        });

        votersContacted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), ViewVotersContacted.class));
                Toast.makeText(AdminHome.this, "Getting voters contacted....", Toast.LENGTH_SHORT).show();
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(userAuth.getUid());
            }
        });

        partyStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Integer> numberofVoters = new ArrayList<>();
                firebaseFirestore.collection("party").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot snapshot : task.getResult()){
                                numberofVoters.add(Integer.parseInt(snapshot.getData().get("voterCount").toString()));
                            }
                        }
                        PartyStatistics partstats = new PartyStatistics(numberofVoters);
                        startActivity(new Intent(getApplicationContext(), partstats.getClass()));
                    }
                });
            }
        });


        firebaseFirestore.collection("canvassers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        if(currentUser.getUid().equals(snapshot.getData().get("authenticationID"))){
                            firstName = snapshot.getData().get("firstName").toString();
                            greetingUser.append(" "+firstName + "!");
                            Log.d(TAG, "user: "+greetingUser.toString());
                        }
                    }
                }
            }
        });
        canvassersToApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewCanvassers.class));
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });

        firebaseFirestore.collection("canvassersAwaitingApproval").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        Log.d(TAG, "CANVASSERS WAITING: "+snapshot.getData().get("firstName")+" - "+snapshot.getData().get("email"));
                    }
                }
                if(!task.isSuccessful()){
                 }
            }
        });
        Toast.makeText(this, "User's name: " + userAuth.getUid()+": ", Toast.LENGTH_SHORT).show();

    }

    private void openDialog(String authenticationID){
        AdminProfileDialog adminProfileDialog = new AdminProfileDialog(authenticationID);
        adminProfileDialog.show(getSupportFragmentManager(), null);
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
        startActivity(new Intent(getApplicationContext(), AdminHome.class));
    }

    public void homePage(){
        startActivity(new Intent(getApplicationContext(), AdminHome.class));
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    public void accountInfo(){
        Toast.makeText(AdminHome.this, "User: "+userAuth.getUid()+"\nEmail: "+userAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
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
    public void applyAdminLocationDataUpdate(String authenticationID, String firstName, String lastName, String districtOfWork, String areaOfDirstrict, String docRef) {
        DocumentReference documentReference = firebaseFirestore.collection(CANVASSERS_TABLE).document(docRef);
        Map<String, Object> dataToUpdate = new HashMap<>();
        dataToUpdate.put("district", districtOfWork);
        dataToUpdate.put("area", areaOfDirstrict);

        documentReference.update(dataToUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "SUCCESS: District data and area data have been updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "ERROR: "+e.getMessage());
            }
        });
    }

    //This method gets the raw csv data and puts it in the firestore database
    /*private void readData() throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.voterdatas);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("UTF-8"))
        );
        String line = "";
        try{
            reader.readLine();

            while((line = reader.readLine()) != null){
                String[] tokens = line.split(",");
                Log.d(TAG, tokens.toString());
                //Read data
                Voters sample = new Voters();
                sample.setENP(tokens[0]);
                sample.setEN(Integer.parseInt(tokens[1]));
                sample.setENS(Integer.parseInt(tokens[2]));
                sample.setLastName(tokens[3]);
                sample.setFirstName(tokens[4]);
                sample.setPostCode(tokens[5]);
                sample.setAddress1(tokens[6]);
                sample.setAddress2(tokens[7]);
                sample.setCity(tokens[8]);
                sample.setContacted(Boolean.valueOf(tokens[9]));
                sample.setPartyChosen(tokens[10]);
                sample.setVoterContactedBy(tokens[11]);
                list.add(sample);
                Log.d("AdminHome", "Just created: "+sample);
            }
        }catch (IOException e){
            Log.wtf("MyActivity: Error reading data from file"+line, e);
            e.printStackTrace();
        }

        HashMap<String, Object> inputData = new HashMap<>();
        Voters voterSample = new Voters();
        for(int i = 0; i < list.size(); i++){
            inputData.put("enp", list.get(i).getENP());
            inputData.put("en", list.get(i).getEN());
            inputData.put("ens", list.get(i).getENS());
            inputData.put("lastName", list.get(i).getLastName());
            inputData.put("firstName", list.get(i).getFirstName());
            inputData.put("postalCode", list.get(i).getPostalCode());
            inputData.put("address1", list.get(i).getAddress1());
            inputData.put("address2", list.get(i).getAddress2());
            inputData.put("city", list.get(i).getCity());
            inputData.put("contacted", list.get(i).isContacted());
            inputData.put("partyChosen", list.get(i).getPartyChosen());
            inputData.put("voterContactedBy", list.get(i).getVoterContactedBy());

            votersData.add(inputData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(AdminHome.this, "Sign Up successful, now you will have to wait for approval", Toast.LENGTH_SHORT).show();
                        Log.d("MyActivity", "Data has been entered into Firestore");
                    }
                    else if(!task.isSuccessful()){
                        Log.d("MyActivity", "ERROR: Data couldn't be entered into firestore");
                    }
                }
            });
        }
    }*/

}




