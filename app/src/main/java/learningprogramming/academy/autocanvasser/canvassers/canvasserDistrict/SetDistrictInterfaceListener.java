package learningprogramming.academy.autocanvasser.canvassers.canvasserDistrict;

public interface SetDistrictInterfaceListener {
    void applyInformation(String id, String fullName, String emailAddress, String district, String areaOfDistrict, String documentRef);
    void cancelDialog();
}
