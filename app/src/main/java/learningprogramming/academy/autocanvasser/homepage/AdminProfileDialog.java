package learningprogramming.academy.autocanvasser.homepage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import learningprogramming.academy.autocanvasser.R;

public class AdminProfileDialog extends AppCompatDialogFragment {

    private String authenticationID, documentRef;

    private TextView fullName, emailAddress, role, party, currentAreaOfDistrict, currentDistrict;
    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;

    private ArrayAdapter<String> areaSpinner;
    private ArrayAdapter<String> districtSpinner;
    private Spinner newAreaOfDistrict, newDistrict;
    private HomeInformation adminHomeInfo;

    public static final String BETHNAL_GREEN = "Bethnal Green";
    public static final String STEPNEY_GREEN = "Stepney Green";
    public static final String MILE_END = "Mile End";

    public static final String CANVASSERS_TABLE = "canvassers";
    private static final String TAG = "AdminProfileDialog";


    public AdminProfileDialog(String userAuthenId){ }

    public AdminProfileDialog(){ }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        View view = layoutInflater.inflate(R.layout.activity_admin_profile_dialog, null);

        authenticationID = userAuth.getUid();

        fullName = view.findViewById(R.id.adminFullName);
        emailAddress = view.findViewById(R.id.adminEmail);
        role = view.findViewById(R.id.adminRole);
        currentDistrict = view.findViewById(R.id.districtId);
        currentAreaOfDistrict = view.findViewById(R.id.areaId);

        newDistrict = view.findViewById(R.id.adminDistrict);
        newAreaOfDistrict = view.findViewById(R.id.adminArea);
        party = view.findViewById(R.id.adminParty);


        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(authenticationID)){
                            fullName.setText(snapshot.getData().get("firstName").toString());
                            fullName.append(" "+snapshot.getData().get("lastName").toString());
                            emailAddress.setText(snapshot.getData().get("email").toString());
                            role.setText("Manager");
                            party.setText(snapshot.getData().get("party").toString());
                            currentDistrict.setText(snapshot.getData().get("district").toString());
                            currentAreaOfDistrict.setText(snapshot.getData().get("area").toString());
                            documentRef = snapshot.getId();
                        }
                    }
                }
            }
        });

        districtSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.areaToCanvas));
        newDistrict.setAdapter(districtSpinner);

        newDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String area = parent.getItemAtPosition(position).toString();
                switch (area){
                    case BETHNAL_GREEN:
                        areaSpinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.BethnalGreenZones));
                        break;
                    case STEPNEY_GREEN:
                        areaSpinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.StepneyGreenZones));
                        break;
                    case MILE_END:
                        areaSpinner = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.MileEndZones));
                        break;
                    default:
                        areaSpinner = null;
                }
                newAreaOfDistrict.setAdapter(areaSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String firstName = fullName.getText().toString().split(" ")[0];
                String lastName = fullName.getText().toString().split(" ")[1];
                String districtOfWork = newDistrict.getSelectedItem().toString();
                String areaOfWork = newAreaOfDistrict.getSelectedItem().toString();
                adminHomeInfo.applyAdminLocationDataUpdate(authenticationID, firstName, lastName, districtOfWork, areaOfWork, documentRef);
            }
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try{
            super.onAttach(context);
            adminHomeInfo = (HomeInformation) context;
        }catch (ClassCastException e){
            Log.d(TAG, "ERROR: "+e.getMessage());
        }

    }
}
