package learningprogramming.academy.autocanvasser.canvassers.dialog.popup;

public interface CanvasserDialogListener {
    void approveUser(int id, String fullName, String emailAddress, String politicalParty, String documentReference);
    void rejectUser(int id, String fullName, String emailAddress, String documentReference);
    void cancelDialog();
}
