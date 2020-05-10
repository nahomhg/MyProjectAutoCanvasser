package learningprogramming.academy.autocanvasser.canvassers;

public class Canvassers {

    private String authenticationID;
    private String firstName;
    private String lastName;
    private String email;
    private String party;


    public Canvassers(String firstNameParam, String lastNameParam, String emailAddress, String politicalPartyParam) {
        this.firstName = firstNameParam;
        this.lastName = lastNameParam;
        this.email = emailAddress;
        this.party = politicalPartyParam;
    }

    public Canvassers(String authenticationId, String firstNameParam, String lastNameParam, String emailAddress, String politicalPartyParam) {
        this.authenticationID = authenticationId;
        this.firstName = firstNameParam;
        this.lastName = lastNameParam;
        this.email = emailAddress;
        this.party = politicalPartyParam;
    }

    public Canvassers(){

    }

    public String getAuthenticationId() {
        return this.authenticationID;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationID = authenticationId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String politicalParty) {
        this.party = politicalParty;
    }


    @Override
    public String toString() {
        return "Canvassers{" +
                ", authenticationId='" + authenticationID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", party='" + party + '\'' +
                '}';
    }
}

