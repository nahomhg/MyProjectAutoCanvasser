package learningprogramming.academy.autocanvasser.canvassers.canvassersViewDistrict;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import learningprogramming.academy.autocanvasser.homepage.HomeInformation;

public class ViewCanvasserDistrict extends AppCompatDialogFragment {

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


    public ViewCanvasserDistrict(String userAuthenId){ }

    public ViewCanvasserDistrict(){ }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        View view = layoutInflater.inflate(R.layout.activity_view_canvasser_district, null);

        authenticationID = userAuth.getUid();

        fullName = view.findViewById(R.id.myDistrictFullName);
        currentDistrict = view.findViewById(R.id.myDistrictDistrictId);
        currentAreaOfDistrict = view.findViewById(R.id.myDistrictAreaId);

        party = view.findViewById(R.id.canvasserParty);


        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(authenticationID)){
                            fullName.setText(snapshot.getData().get("firstName").toString());
                            fullName.append(" "+snapshot.getData().get("lastName").toString());
                            currentDistrict.setText(snapshot.getData().get("district").toString());
                            currentAreaOfDistrict.setText(snapshot.getData().get("area").toString());
                            documentRef = snapshot.getId();
                        }
                    }
                }
            }
        });

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
