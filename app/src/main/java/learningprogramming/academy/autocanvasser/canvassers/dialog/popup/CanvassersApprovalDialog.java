package learningprogramming.academy.autocanvasser.canvassers.dialog.popup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.appcompat.widget.SwitchCompat;
import learningprogramming.academy.autocanvasser.R;

public class CanvassersApprovalDialog extends AppCompatDialogFragment {

    private TextView fullName;
    private TextView emailAddress;
    private TextView partyChosen;
    private String documentReference;
    private SwitchCompat isAdmin;

    public static final String CANVASSERS_TABLE = "canvassers";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private int canvasserID;
    private String canvasserAuthenticationID;
    private CanvasserDialogListener canvasserDialogListener;
    private boolean dialogClosed = false;
    private static final String TAG = "CanApprovalDialog";

    public CanvassersApprovalDialog(String id){
        this.canvasserAuthenticationID = id;
    }

    public CanvassersApprovalDialog(){
        //do nothing
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        View view = layoutInflater.inflate(R.layout.activity_approve_canvassers_dialog, null);

        fullName = view.findViewById(R.id.fullName);
        emailAddress = view.findViewById(R.id.passwordResetEmail);
        partyChosen = view.findViewById(R.id.approvalParty);

        firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        if(snapshot.getData().get("authenticationID").toString().equals(canvasserAuthenticationID)){
                            fullName.setText(snapshot.getData().get("firstName").toString());
                            fullName.append(" "+snapshot.getData().get("lastName").toString());
                            emailAddress.setText(snapshot.getData().get("email").toString());
                            partyChosen.setText(snapshot.getData().get("party").toString());
                            documentReference = snapshot.getId();
                        }
                    }
                }
            }
        });

        dialogBuilder.setView(view);

        dialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialogBuilder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogClosed = true;
                canvasserDialogListener.rejectUser(canvasserID,fullName.getText().toString(),emailAddress.getText().toString(),documentReference);
            }
        });
        dialogBuilder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userfullname = fullName.getText().toString();
                String emailAddrss = emailAddress.getText().toString();
                String docRef = documentReference.toString();
                String politicalParty = partyChosen.getText().toString();
                canvasserDialogListener.approveUser(canvasserID,userfullname, emailAddrss, politicalParty, docRef);
            }
        });
        return dialogBuilder.create();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAttach(@NonNull Context context) {
        try{
            super.onAttach(context);
            canvasserDialogListener = (CanvasserDialogListener) context;
        }catch (ClassCastException e){
            Log.d(TAG, "ERROR: "+e.getMessage());
        }
    }

    public boolean getDialogStatus(){
        return this.dialogClosed;
    }
}
