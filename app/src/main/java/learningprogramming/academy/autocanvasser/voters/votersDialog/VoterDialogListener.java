package learningprogramming.academy.autocanvasser.voters.votersDialog;

public interface VoterDialogListener {
    void applyVoterInformation(int electoralNumber, int electoralNumberSuffix, String electoralNumberPrefix,
                               String fullName, String address, String city, String postCode, String partyChoice,
                               String[] voterQuestions, String documentReference, String contactedByCanvasserName, String contactIno);
    void cancelDialog();
}