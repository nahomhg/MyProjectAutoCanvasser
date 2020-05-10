package learningprogramming.academy.autocanvasser.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.userLoginRegister.login.UserLogin;
import learningprogramming.academy.autocanvasser.canvassers.canvassersViewDistrict.ViewCanvasserDistrict;
import learningprogramming.academy.autocanvasser.partyStats.PartyStatistics;
import learningprogramming.academy.autocanvasser.voters.ViewVoters;
import learningprogramming.academy.autocanvasser.voters.contacted.ViewVotersContacted;

public class CanvasserHome extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private Button startCanvassing;
    private Button settingsBtn;
    private Button votersContacted;
    private TextView greetingUser;
    private Button profileBtn;
    private Button partyStatistics;
    private Button districtBtn;

    private String firstName;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvasser_home_page);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        startCanvassing = findViewById(R.id.startCanvassingBtn);
        settingsBtn =  findViewById(R.id.settingsBtn);
        votersContacted = findViewById(R.id.votersContactedBtn);
        greetingUser = findViewById(R.id.greetingUser);
        profileBtn = findViewById(R.id.profileBtn);
        partyStatistics = findViewById(R.id.partyStatsBtn);
        districtBtn = findViewById(R.id.myDistrictBtn);

        startCanvassing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewVoters.class));
                Toast.makeText(CanvasserHome.this, "Starting Canvassing...", Toast.LENGTH_SHORT).show();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CanvasserHome.this, "Settings clicked...", Toast.LENGTH_SHORT).show();
            }
        });

        districtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyDistrictDialog(currentUser.getUid());
            }
        });

        votersContacted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewVotersContacted.class));
                Toast.makeText(CanvasserHome.this, "Getting voters contacted....", Toast.LENGTH_SHORT).show();
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileDialog(currentUser.getUid());
            }
        });

        partyStatistics.setOnClickListener(new View.OnClickListener() {
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
                            greetingUser.append(firstName + "!");
                        }
                    }
                }
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });

    }

    private void openProfileDialog(String authenticationID){
        CanvasserProfileDialog adminProfileDialog = new CanvasserProfileDialog(authenticationID);
        adminProfileDialog.show(getSupportFragmentManager(), null);
    }

    private void openMyDistrictDialog(String authenticationID){
        ViewCanvasserDistrict viewCanvasserDistrict = new ViewCanvasserDistrict(authenticationID);
        viewCanvasserDistrict.show(getSupportFragmentManager(), null);
    }

    private void settings(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu);
        popup.show();
    }

    private void signOut() {
        mAuth.signOut();
        finish();

        startActivity(new Intent(getApplicationContext(), UserLogin.class));
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    private void refreshPage(){
        finish();
        startActivity(new Intent(getApplicationContext(), CanvasserHome.class));
    }

    public void accountInfo(){
        Toast.makeText(CanvasserHome.this, "User: "+mAuth.getUid()+"\nEmail: "+mAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutBtn:
                signOut();
                return true;
            case R.id.accountBtn:
                accountInfo();;
                return true;
            case R.id.refreshBtn:
                refreshPage();
                return true;
            default:
                return false;
        }
    }
}
