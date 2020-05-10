package learningprogramming.academy.autocanvasser.voters.votersDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import learningprogramming.academy.autocanvasser.R;

public class VoterDetailsDialog extends AppCompatDialogFragment implements DeleteVoter {

    private int userId;

    private TextView fullName, address, city ,postalCode, questionOne, questionOneLabel, contactInoLabel, contactInfo;
    private TextView electionNumberPrefixInfoHolder, electionNumberHolder;
    private TextView electionNumberPrefixInfo, electionNumber, electionNumberSufixHolder, electionNumberSufix;
    private Button deleteUser;

    private boolean dialogClosed = false;

    private Spinner party;
    private SwitchCompat wouldLikeToAskQuestions, wouldGiveContactInfo;

    private String documentRef;
    private static final String TAG = "VoterActivity";
    public static final String VOTERS_TABLE = "voters";

    private VoterDialogListener voterDialogListener;
    //private HashMap<String, String> userData = new HashMap<>();

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth userAuth;

    public VoterDetailsDialog(int id){
        this.userId = id;
    }

    public VoterDetailsDialog(){

    }

    @Override
    public Dialog onCreateDialog(@NonNull Bundle bundleCreator){

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.activity_voter_details_pop_up, null);

        //User data
        fullName = view.findViewById(R.id.fullName);
        party = view.findViewById(R.id.partySelectorSpinner);
        address = view.findViewById(R.id.addressHolder);
        city = view.findViewById(R.id.userCity);
        postalCode = view.findViewById(R.id.userPostCode);
        electionNumberPrefixInfoHolder = view.findViewById(R.id.electionNumberPrefixInfoHolder);
        electionNumberHolder = view.findViewById(R.id.electionNumberHolder);
        electionNumberPrefixInfo = view.findViewById(R.id.electionNumberPrefixInfo);
        electionNumber = view.findViewById(R.id.electionNumber);
        electionNumberSufixHolder = view.findViewById(R.id.electionNumberSufixHolder);
        electionNumberSufix = view.findViewById(R.id.electionNumberSufix);
        //User's questions
        wouldLikeToAskQuestions = view.findViewById(R.id.questionToggle);
        questionOne = view.findViewById(R.id.questionOne);
        questionOneLabel = view.findViewById(R.id.questionLabel);

        //User give contact info:
        wouldGiveContactInfo = view.findViewById(R.id.giveContactInformation);
        contactInoLabel = view.findViewById(R.id.contactInfoHolder);
        contactInfo = view.findViewById(R.id.contactInfo);

        //Delete User Button
        deleteUser = view.findViewById(R.id.deleteUserBtn);

        contactInoLabel.setVisibility(View.INVISIBLE);
        contactInfo.setVisibility(View.INVISIBLE);
        questionOne.setVisibility(View.INVISIBLE);
        questionOneLabel.setVisibility(View.INVISIBLE);

        final ArrayAdapter<String> partyAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.politicalParty));

        partyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        firebaseFirestore.collection("voters").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()) {
                        Log.d(TAG, "USER ID: " + snapshot.getId() + ", " + snapshot.getData().get("en"));
                        if (snapshot.getData().get("en").toString().equals(String.valueOf(userId))) {
                            fullName.setText(snapshot.getData().get("firstName").toString());
                            fullName.append(" "+snapshot.getData().get("lastName").toString());
                            address.setText(snapshot.getData().get("address1").toString());
                            address.append(", "+snapshot.getData().get("address2").toString());
                            city.setText(snapshot.getData().get("city").toString());
                            postalCode.setText(snapshot.getData().get("postalCode").toString());
                            electionNumberPrefixInfo.setText(snapshot.getData().get("enp").toString());
                            electionNumber.setText(snapshot.getData().get("en").toString());
                            electionNumberSufix.setText(snapshot.getData().get("ens").toString());
                            party.setAdapter(partyAdapter);
                            documentRef = snapshot.getId();
                            Log.d(TAG, "This (" + snapshot.getData().get("firstName") + ") document id: " + snapshot.getId());
                        }
                    }
                }
            }
        });

        wouldLikeToAskQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wouldLikeToAskQuestions.isChecked()){
                    questionOne.setVisibility(View.VISIBLE);
                    questionOneLabel.setVisibility(View.VISIBLE);
                }else if(!wouldLikeToAskQuestions.isChecked()){
                    questionOne.setVisibility(View.INVISIBLE);
                    questionOneLabel.setVisibility(View.INVISIBLE);
                }
            }
        });

        wouldGiveContactInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wouldGiveContactInfo.isChecked()){
                    contactInoLabel.setVisibility(View.VISIBLE);
                    contactInfo.setVisibility(View.VISIBLE);
                }else if(!wouldGiveContactInfo.isChecked()){
                    contactInoLabel.setVisibility(View.INVISIBLE);
                    contactInfo.setVisibility(View.INVISIBLE);
                }
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteVoterFromDatabase(VOTERS_TABLE, documentRef);
            }
        });

        builder.setView(view);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                voterDialogListener.cancelDialog();
            }
        });


        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int voterElectionNumber = Integer.parseInt(electionNumber.getText().toString());
                int voterElectionNumberSufix = Integer.parseInt(electionNumberSufix.getText().toString());
                String voterElectionNumberPrefix = electionNumberPrefixInfo.getText().toString();
                String voterName = fullName.getText().toString();
                String voterAddress = address.getText().toString();
                String userCity = city.getText().toString();
                String pstCde = postalCode.getText().toString();
                String[] questions = questionOne.getText().toString().split("\\.");
                String contactInformation = contactInfo.getText().toString();
                String partyChoice = party.getSelectedItem().toString();
                String documentId = documentRef.toString();
                String canvasserContactedBy = userAuth.getUid();
                voterDialogListener.applyVoterInformation(voterElectionNumber, voterElectionNumberSufix, voterElectionNumberPrefix,
                        voterName, voterAddress, userCity, pstCde, partyChoice ,questions,
                        documentId, canvasserContactedBy, contactInformation);
                dialogClosed = true;
            }
        });

        return builder.create();
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

    public boolean getDialogStatus(){
        return this.dialogClosed;
    }

    @Override
    public void deleteVoterFromDatabase(final String collectionName, final String documentReference){
        new AlertDialog.Builder(getContext()).setTitle("Delete entry").setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseFirestore.collection(collectionName).document(documentReference).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "USER HAS BEEN DELETED");
                                    Toast.makeText(getContext(), "Press \'CANCEL\' to close this box", Toast.LENGTH_SHORT).show();

                                }else if(!task.isSuccessful())
                                    Log.d(TAG, "USER HAS NOT BEEN DELETED");
                            }
                        });
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("NO", null)
                .setIcon(R.drawable.ic_warning_yellow_28dp)
                .show();
    }
}
