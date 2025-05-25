package entities;

public class FactureDetails {
    private int quantite;
    private Medicament medicament;
    private Facture facture;

    public FactureDetails() {}

    public FactureDetails(int quantite, Medicament medicament, Facture facture) {
        this.quantite = quantite;
        this.medicament = medicament;
        this.facture = facture;
    }

    public FactureDetails(Medicament medicament, int quantite) {
        this.quantite = quantite;
        this.medicament = medicament;
    }

    // Getters et setters
    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Medicament getMedicament() {
        return medicament;
    }

    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }

    public double getSousTotal() {
        return medicament != null ? quantite * medicament.getPrix_unitaire() : 0;
    }

    public void setPrixUnitaire(double prixUnitaire) {
    }




    // Corrigez les méthodes pour prix_unitaire


    public void setPrix_unitaire(double prixUnitaire) {
        // Ne rien faire ou mettre à jour le médicament si nécessaire
    }
    public double getPrix_unitaire() {
        return medicament != null ? medicament.getPrix_unitaire() : 0;
    }
}