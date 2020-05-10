package learningprogramming.academy.autocanvasser.partyStats;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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

public class PartyStatistics extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "PartyStats";
    private String currentUserUid;
    private PieChart pieChart;
    private Button settingsBtn;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private static final String VOTERS_CONTACTED_TABLE = "voterscontacted";
    private static final String PARTY_TABLE = "party";
    private static List<Integer> numberOfVoters = new ArrayList<>();
    private static ArrayList<PieEntry> values;

    public PartyStatistics(){
    }

    public PartyStatistics(List<Integer> numberOfVoters){ this.numberOfVoters = numberOfVoters; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_statistics);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = firebaseAuth.getUid();
        values = new ArrayList<>();

        pieChart = findViewById(R.id.pieChart);
        settingsBtn = findViewById(R.id.settingsBtn);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings(v);
            }
        });

        Log.d(TAG, "hello worlld: "+numberOfVoters);

        createChart(numberOfVoters);
    }
    public void createChart(List<Integer> items){
        values = new ArrayList<>();

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);

        pieChart.setTransparentCircleRadius(61);

        values.add(new PieEntry(items.get(0), "Conservatives"));
        values.add(new PieEntry(items.get(1), "Green Party"));
        values.add(new PieEntry(items.get(2), "Labour"));
        values.add(new PieEntry(items.get(3), "Liberal Democrats"));
        values.add(new PieEntry(items.get(4), "Unsure"));

        pieChart.animateY(1250, Easing.EaseInOutCubic);

        PieDataSet dataSet = new PieDataSet(values, "");
        dataSet.setSliceSpace(5f);
        dataSet.setSelectionShift(7f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(15f);
        data.setValueTextColor(Color.WHITE);

        Description chartDescription = new Description();
        chartDescription.setText("The data represents (in percentages) who the contacted voters have said they will vote for");
        chartDescription.setTextSize(10);
        pieChart.setDescription(chartDescription);

        Log.d(TAG, "sum of y values: "+pieChart.getData());

        pieChart.setData(data);
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
        startActivity(new Intent(getApplicationContext(), PartyStatistics.class));
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