package entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Medicament {
    private final IntegerProperty id_Med = new SimpleIntegerProperty();
    private final StringProperty code_barre = new SimpleStringProperty();
    private final StringProperty nom_Med = new SimpleStringProperty();
    private final StringProperty forme_pharmaceutique = new SimpleStringProperty();
    private final StringProperty dosage = new SimpleStringProperty();
    private final DoubleProperty prix_unitaire = new SimpleDoubleProperty();
    private final IntegerProperty stock_dispo = new SimpleIntegerProperty();
    private final BooleanProperty remboursable = new SimpleBooleanProperty();
    private final ObjectProperty<Date> date_ajout = new SimpleObjectProperty<>();

    // Constructeurs
    public Medicament() {
        this.date_ajout.set(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
    }

    public Medicament(String code_barre, String nom_Med, String forme_pharmaceutique,
                      String dosage, double prix_unitaire, int stock_dispo, boolean remboursable) {
        this();
        this.code_barre.set(code_barre);
        this.nom_Med.set(nom_Med);
        this.forme_pharmaceutique.set(forme_pharmaceutique);
        this.dosage.set(dosage);
        this.prix_unitaire.set(prix_unitaire);
        this.stock_dispo.set(stock_dispo);
        this.remboursable.set(remboursable);
    }

    // Getters et setters pour id_Med
    public int getId_Med() {
        return id_Med.get();
    }

    public IntegerProperty id_MedProperty() {
        return id_Med;
    }

    public void setId_Med(int id_Med) {
        this.id_Med.set(id_Med);
    }

    // Getters et setters pour code_barre
    public String getCode_barre() {
        return code_barre.get();
    }

    public StringProperty code_barreProperty() {
        return code_barre;
    }

    public void setCode_barre(String code_barre) {
        this.code_barre.set(code_barre);
    }

    // Getters et setters pour nom_Med
    public String getNom_Med() {
        return nom_Med.get();
    }

    public StringProperty nom_MedProperty() {
        return nom_Med;
    }

    public void setNom_Med(String nom_Med) {
        this.nom_Med.set(nom_Med);
    }

    // Getters et setters pour forme_pharmaceutique
    public String getForme_pharmaceutique() {
        return forme_pharmaceutique.get();
    }

    public StringProperty forme_pharmaceutiqueProperty() {
        return forme_pharmaceutique;
    }

    public void setForme_pharmaceutique(String forme_pharmaceutique) {
        this.forme_pharmaceutique.set(forme_pharmaceutique);
    }

    // Getters et setters pour dosage
    public String getDosage() {
        return dosage.get();
    }

    public StringProperty dosageProperty() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage.set(dosage);
    }

    // Getters et setters pour prix_unitaire
    public double getPrix_unitaire() {
        return prix_unitaire.get();
    }

    public DoubleProperty prix_unitaireProperty() {
        return prix_unitaire;
    }

    public void setPrix_unitaire(double prix_unitaire) {
        this.prix_unitaire.set(prix_unitaire);
    }

    // Getters et setters pour stock_dispo
    public int getStock_dispo() {
        return stock_dispo.get();
    }

    public IntegerProperty stock_dispoProperty() {
        return stock_dispo;
    }

    public void setStock_dispo(int stock_dispo) {
        this.stock_dispo.set(stock_dispo);
    }

    // Getters et setters pour remboursable
    public boolean isRemboursable() {
        return remboursable.get();
    }

    public BooleanProperty remboursableProperty() {
        return remboursable;
    }

    public void setRemboursable(boolean remboursable) {
        this.remboursable.set(remboursable);
    }

    // Getters et setters pour date_ajout
    public Date getDate_ajout() {
        return date_ajout.get();
    }

    public ObjectProperty<Date> date_ajoutProperty() {
        return date_ajout;
    }

    public void setDate_ajout(Date date_ajout) {
        this.date_ajout.set(date_ajout);
    }

    @Override
    public String toString() {
        return nom_Med.get();
    }


    public String getNom() {
        return null;
    }
}
