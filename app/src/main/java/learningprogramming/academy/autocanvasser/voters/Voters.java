package learningprogramming.academy.autocanvasser.voters;

import java.util.List;

public class Voters {
    private int id, EN, ENS;
    private String ENP, firstName, lastName, address1, address2,
            postalCode, city, voterContactedBy, contactInfo, partyChosen;
    private List<String> questions;
    private boolean contacted;

    public Voters(){

    }

    public Voters(int id, int EN, int ENS, String ENP, String firstName,
                  String lastName, String address1, String address2, String postCode, String city) {
        this.id = id;
        this.EN = EN;
        this.ENS = ENS;
        this.ENP = ENP;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address1 = address1;
        this.address2 = address2;
        this.postalCode = postCode;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFullAddress() {
        return getAddress1() +", "+getAddress2()+"\n"+getCity()+", "+ getPostalCode();
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }


    public String getPostalCode(){
        return postalCode;
    }
    public String getCity(){
        return city;
    }
    public void setPostCode(String postCode) {
        this.postalCode = postCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getEN() {
        return EN;
    }

    public void setEN(int EN) {
        this.EN = EN;
    }

    public int getENS() {
        return ENS;
    }

    public void setENS(int ENS) {
        this.ENS = ENS;
    }

    public String getENP() {
        return ENP;
    }

    public void setENP(String ENP) {
        this.ENP = ENP;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public String getVoterContactedBy() {
        return voterContactedBy;
    }

    public void setVoterContactedBy(String voterContactedBy) {
        this.voterContactedBy = voterContactedBy;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isContacted() {
        return contacted;
    }

    public void setContacted(boolean contacted) {
        this.contacted = contacted;
    }

    public String getPartyChosen() {
        return partyChosen;
    }

    public void setPartyChosen(String partyChosen) {
        this.partyChosen = partyChosen;
    }

    @Override
    public String toString() {
        return "Voters{" +
                "id=" + id +
                ", EN=" + EN +
                ", ENS=" + ENS +
                ", ENP='" + ENP + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", voterContactedBy='" + voterContactedBy + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", questions=" + questions +
                ", contacted="+contacted+
                '}';
    }
}
