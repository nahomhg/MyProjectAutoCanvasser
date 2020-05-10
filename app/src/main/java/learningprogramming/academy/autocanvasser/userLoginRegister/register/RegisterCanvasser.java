package learningprogramming.academy.autocanvasser.userLoginRegister.register;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import learningprogramming.academy.autocanvasser.R;

public class RegisterCanvasser extends AppCompatDialogFragment {

    private Spinner partySelector;
    private EditText firstName;
    private EditText lastName;
    private EditText email, confirmEmail;

    private FirebaseAuth userAuth;
    private FirebaseFirestore firebaseFirestore;
    //private EditText partySelector;
    private RegisterUserDialogListener listener;

    private static int id = 1000;
    private static final String CANVASSERS_AWAITING_TABLE = "canvassersAwaitingApproval";
    private static final String CANVASSERS_TABLE = "canvassers";
    public static final String TAG = "RegisterCanvassers";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        userAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.dialog_register_user, null);

        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);
        email = view.findViewById(R.id.passwordResetEmail);
        confirmEmail = view.findViewById(R.id.emailConfirm);
        partySelector = view.findViewById(R.id.partySelectorSpinner);

        ArrayAdapter<String> partyAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.politicalPartyForCanvasser));
        partyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        partySelector.setAdapter(partyAdapter);

        builder.setView(view);

        partySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "SOMETHING'S HAPPENING");
                firebaseFirestore.collection(CANVASSERS_TABLE).whereEqualTo("email", email.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            if (snapshot.exists()) {
                               Log.d(TAG, "TESTING EMAIL "+email.getText().toString());
                               email.setError("User already exists");
                               confirmEmail.setError("User already exists");
                               confirmEmail.setText(" ");

                            }else if(!snapshot.exists()){
                                Log.d(TAG, "USER DOESN\'T EXIST YAY ");
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "HELLO NAHOM, nothign has been done");

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //No code needed as the dialog will close by itself
            }
        });

        builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                id += 1;
                final String fname = firstName.getText().toString();
                final String lname = lastName.getText().toString();
                final String emailAddress = email.getText().toString();
                final String confirmedEmailAddress = confirmEmail.getText().toString();
                final String partySelected = partySelector.getSelectedItem().toString();
                if(validatePartyChoice(partySelected) || inputNamesValidator(fname, lname)) {
                    if (registerEmailValidator(emailAddress, confirmedEmailAddress) && emailAddress.equals(confirmedEmailAddress)) {
                        //TODO: Make sure that users cannot register the same email twice!!!!*****
                        listener.createUserAccount(id, fname, lname, confirmedEmailAddress, partySelected);
                        dismiss();
                    } else if (!registerEmailValidator(emailAddress, confirmedEmailAddress))
                        Toast.makeText(getContext(), "Invalid email address, emails do not match", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getContext(), "Invalid choice of party or entry of details", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    private boolean inputNamesValidator(String firstName, String lastName){
        if(firstName.isEmpty() || lastName.isEmpty()) return false;
        return true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            super.onAttach(context);
            listener = (RegisterUserDialogListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString()+ " : Must implement register dialog listener");
        }
    }

    private boolean validatePartyChoice(String partySelected){
        if(partySelected.equals("Select A Party")){
            return false;
        }
        return true;
    }


    public boolean registerEmailValidator(String email, String confirmEmailAddress) {
        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; //Code from TutorialPoint;

        if(email.isEmpty() || confirmEmailAddress.isEmpty()) {
            Toast.makeText(getContext(), "Please enter an email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!email.equals(confirmEmailAddress))
            return false;

        else if(email.trim().matches(emailRegex) && confirmEmailAddress.trim().matches(emailRegex))
                return true;

        return false;
    }

   /* private boolean checkEmailInApprovalAwaitingTable(final String emailAddress){
        Query query = firebaseFirestore.collection(CANVASSERS_AWAITING_TABLE).whereEqualTo("email", emailAddress)
    }*/
}
