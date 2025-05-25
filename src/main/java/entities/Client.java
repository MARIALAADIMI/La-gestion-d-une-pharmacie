package entities;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Client {
    private final StringProperty CIN = new SimpleStringProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty tele = new SimpleStringProperty();
    private final StringProperty adresse = new SimpleStringProperty();
    private final ObjectProperty<Date> dateInscription = new SimpleObjectProperty<>();

    // Constructeurs
    public Client() {
        this.dateInscription.set(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    public Client(String CIN, String nom, String prenom, String tele, String adresse) {
        this();
        this.CIN.set(CIN);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.tele.set(tele);
        this.adresse.set(adresse);
    }

    // Getters et setters pour CIN
    public String getCIN() {
        return CIN.get();
    }

    public StringProperty CINProperty() {
        return CIN;
    }

    public void setCIN(String CIN) {
        this.CIN.set(CIN);
    }

    // Getters et setters pour nom
    public String getNom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    // Getters et setters pour prenom
    public String getPrenom() {
        return prenom.get();
    }

    public StringProperty prenomProperty() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom.set(prenom);
    }

    // Getters et setters pour tele
    public String getTele() {
        return tele.get();
    }

    public StringProperty teleProperty() {
        return tele;
    }

    public void setTele(String tele) {
        this.tele.set(tele);
    }

    // Getters et setters pour adresse
    public String getAdresse() {
        return adresse.get();
    }

    public StringProperty adresseProperty() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse.set(adresse);
    }

    // Getters et setters pour dateInscription
    public Date getDateInscription() {
        return dateInscription.get();
    }

    public ObjectProperty<Date> dateInscriptionProperty() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription.set(dateInscription);
    }

    @Override
    public String toString() {
        return nom.get() + " " + prenom.get();
    }

    public String getCin() {
        return null;
    }

}