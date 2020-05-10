package learningprogramming.academy.autocanvasser.voters.contacted;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import learningprogramming.academy.autocanvasser.R;
import learningprogramming.academy.autocanvasser.voters.Voters;
import learningprogramming.academy.autocanvasser.voters.votersDialog.DeleteVoter;
import learningprogramming.academy.autocanvasser.voters.votersDialog.VoterDialogListener;

public class VotersContactedDialog extends AppCompatDialogFragment implements DeleteVoter {

    private static final String TAG = "VoterContacted";
    private FirebaseFirestore firebaseFirestore;

    private int voterElectoralNumber;

    private static final String CANVASSERS_TABLE = "canvassers";
    private static final String VOTERS_TABLE = "voters";
    private static final String PARTY_TABLE = "party";
    private static final String VOTERS_CONTACTED_TABLE = "voterscontacted";

    private VoterDialogListener voterDialogListener;

    private TextView fullName, address, electoralNumber,
            electoralNumebrPrefix, questions, contactedBy, contactInformation;
    private String voterPartyOfChoice;
    private String documentReference;
    private Button deleteVoterButton;
    public VotersContactedDialog(){}

    public VotersContactedDialog(int voterElectoralNumber){
        this.voterElectoralNumber = voterElectoralNumber;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        View view = layoutInflater.inflate(R.layout.activity_voters_contacted_dialog, null);

        fullName = view.findViewById(R.id.canvassersFullName);
        address = view.findViewById(R.id.canvassersAddress);
        electoralNumber = view.findViewById(R.id.canvasserElectroalNumber);
        electoralNumebrPrefix = view.findViewById(R.id.canvasserElectroalNumberPrefix);
        questions = view.findViewById(R.id.canvasserQuestion);
        contactedBy = view.findViewById(R.id.canvasserContactedBy);
        contactInformation = view.findViewById(R.id.canvasserContactInfo);
        deleteVoterButton = view.findViewById(R.id.deleteVoterBtn);

        firebaseFirestore.collection(VOTERS_CONTACTED_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    String votersQuestions = "";
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if(documentSnapshot.getData().get("en").toString().equals(String.valueOf(voterElectoralNumber))){
                            Voters votersInformation = documentSnapshot.toObject(Voters.class);
                            fullName.setText(documentSnapshot.getData().get("firstName").toString());
                            fullName.append(" "+documentSnapshot.getData().get("lastName").toString());
                            address.setText(documentSnapshot.getData().get("address1").toString());
                            address.append("\n"+documentSnapshot.getData().get("address2").toString());
                            address.append("\n"+documentSnapshot.getData().get("city").toString());
                            address.append(", "+documentSnapshot.getData().get("postalCode").toString());
                            contactInformation.setText(documentSnapshot.getData().get("contactInfo").toString());
                            electoralNumber.setText(documentSnapshot.getData().get("en").toString());
                            electoralNumebrPrefix.setText(documentSnapshot.getData().get("enp").toString());
                            if(votersInformation.getQuestions()!=null)
                                for(String listOfQuestions : votersInformation.getQuestions())
                                votersQuestions += "\n-" + listOfQuestions;

                            questions.setText(votersQuestions);
                            contactedBy.setText(documentSnapshot.getData().get("voterContactedBy").toString());
                            documentReference = documentSnapshot.getId();
                            final String contactedByInfo = documentSnapshot.getData().get("voterContactedBy").toString();
                            voterPartyOfChoice = documentSnapshot.getData().get("partyChosen").toString();
                            firebaseFirestore.collection(CANVASSERS_TABLE).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot snapshot : task.getResult()){
                                            if(snapshot.getData().get("authenticationID").toString().equals(contactedByInfo)){
                                                contactedBy.setText(snapshot.getData().get("firstName").toString());
                                                contactedBy.append(" "+snapshot.getData().get("lastName").toString());
                                            }
                                        }
                                    }
                                }

                            });
                        }
                    }
                }
            }
        });

        deleteVoterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteVoterFromDatabase(VOTERS_CONTACTED_TABLE, documentReference);
                Log.d(TAG, "DOC refernce:"+documentReference);
            }
        });

        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                voterDialogListener.cancelDialog();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }


    @Override
    public void deleteVoterFromDatabase(final String collectionName, final String documentReference) {
        final Map<String, Object> partyList = new HashMap<>();
        partyList.put("name", voterPartyOfChoice);
        partyList.put("voterCount", FieldValue.increment(-1));

        new AlertDialog.Builder(getContext()).setTitle("Delete entry").setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseFirestore.collection(collectionName).document(documentReference).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "USER HAS BEEN DELETED");
                                    Toast.makeText(getContext(), "Press \'CANCEL\' to close this box", Toast.LENGTH_SHORT).show();

                                    //Remove the person from the total of voters who said they were going to vote for said party.
                                    firebaseFirestore.collection(PARTY_TABLE).document(voterPartyOfChoice).update(partyList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Party total has dropped by one");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "FAILURE: COULDN'T REMOVE PERSON FROM PARTY");
                                        }
                                    });
                                    //End of removal of one vote from the total voters of the party.

                                }else if(!task.isSuccessful())
                                    Log.d(TAG, "USER HAS NOT BEEN DELETED");
                            }
                        });
                    }
                })

                // A null listener allows the button to dismiss the dialog with no further action
                .setNegativeButton("NO", null)
                .setIcon(R.drawable.ic_warning_yellow_28dp)
                .show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try{
            super.onAttach(context);
            voterDialogListener = (VoterDialogListener) context;
        }catch (ClassCastException e){
            Log.d(TAG, "ERROR: "+e.getMessage());
        }
    }
}
