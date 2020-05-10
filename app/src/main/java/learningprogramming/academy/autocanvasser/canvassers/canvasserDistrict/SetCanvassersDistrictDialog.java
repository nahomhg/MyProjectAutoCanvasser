package learningprogramming.academy.autocanvasser.canvassers.canvasserDistrict;

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
import androidx.appcompat.app.AppCompatDialogFragment;
import learningprogramming.academy.autocanvasser.R;

public class SetCanvassersDistrictDialog extends AppCompatDialogFragment {

    private Spinner areaOfDistrict, district;
    private TextView fullName, email;
    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;

    private String userAuthenticationId;
    private String documentReference;

    private static final String TAG = "SetDistrictDialog";
    public static final String BETHNAL_GREEN = "Bethnal Green";
    public static final String STEPNEY_GREEN = "Stepney Green";
    public static final String MILE_END = "Mile End";

    private ArrayAdapter<String> areaSpinner;
    private ArrayAdapter<String> districtSpinner;
    private SetDistrictInterfaceListener districtInterface;


    public SetCanvassersDistrictDialog(String id){
        this.userAuthenticationId = id;
    }

    public SetCanvassersDistrictDialog(){

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        View view = layoutInflater.inflate(R.layout.activity_set_canvassers_district_dialog, null);

        fullName =  view.findViewById(R.id.canvasserFullNameDistrict);
        email = view.findViewById(R.id.emailAddressApprovalDistrict);
        district = view.findViewById(R.id.districtSelectorSpinner);
        areaOfDistrict = view.findViewById(R.id.areaSelectorSpinner);

        firebaseFirestore.collection("canvassers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(String.valueOf(userAuthenticationId))){
                            fullName.setText(snapshot.getData().get("firstName").toString());
                            fullName.append(" "+snapshot.getData().get("lastName").toString());
                            email.setText(snapshot.getData().get("email").toString());
                            documentReference = snapshot.getId();
                            Log.d(TAG, "full name: "+fullName.getText()+", email: "+email.getText());
                        }
                    }
                }
            }
        });

        Log.d(TAG, "Second test "+fullName.getText()+", email: "+email.getText());


        districtSpinner = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.areaToCanvas));
        district.setAdapter(districtSpinner);

        district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                areaOfDistrict.setAdapter(areaSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            //Do nothing
            }
        });



        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                districtInterface.cancelDialog();
            }
        });
        builder.setPositiveButton("Set Area", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = fullName.getText().toString();
                String emailAddress = email.getText().toString();
                String districtAssigned = district.getSelectedItem().toString();
                String areaAssigned = areaOfDistrict.getSelectedItem().toString();
                String documentRef = documentReference;
                Log.d(TAG, "User Id:"+userAuthenticationId+", name: "+userName+", email: "+emailAddress+", district: "+districtAssigned+", area of district: "+areaAssigned+", doc ref: "+documentRef);
                districtInterface.applyInformation(userAuthenticationId, userName, emailAddress, districtAssigned, areaAssigned, documentRef);
            }
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try{
            super.onAttach(context);
            districtInterface = (SetDistrictInterfaceListener) context;
        }catch (ClassCastException e){
            Log.d(TAG, "ERROR: "+e.getMessage());
        }

    }
}
