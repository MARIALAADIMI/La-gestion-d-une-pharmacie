package entities;

import java.sql.Date;
import java.util.List;

public class Facture {
    private int id_Fac;
    private Date date_Fac;
    private double montant_Fac;
    private double montant_total;

    private Client client;

    private List<FactureDetails> details;


    public Facture() {}

    public Facture(int id_Fac, Date date_Fac, Client client) {
        this.id_Fac = id_Fac;
        this.date_Fac = date_Fac;
        this.client = client;
    }

    public Facture(Client client, double montant_Fac, Date date_Fac) {
        this.client = client;
        this.montant_Fac = montant_Fac;
        this.date_Fac = date_Fac;

    }

    // Getters et setters
    public int getId_Fac() {
        return id_Fac;
    }

    public void setId_Fac(int id_Fac) {
        this.id_Fac = id_Fac;
    }





   public double getMontant_Fac() {
        return montant_Fac;
    }

    public void setMontant_Fac(double montant_Fac) {
        this.montant_Fac = montant_Fac;
    }




    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<FactureDetails> getDetails() {
        return details;
    }

    public void setDetails(List<FactureDetails> details) {
        this.details = details;
    }



    // Méthode utilitaire pour calculer le montant à partir des détails
    public void calculerMontant() {
        if (details != null) {
            this.montant_Fac = details.stream()
                    .mapToDouble(FactureDetails::getSousTotal)
                    .sum();
        }
    }







    public Date getDate_Fac() {
        return date_Fac;
    }

    public void setDate_Fac(Date date_Fac) {
        this.date_Fac = date_Fac;
    }

    // Corrigez les getters et setters pour montant_total



    public double getMontant_total() {
        return montant_total;
    }

    public void setMontant_total(double montant_total) {
        this.montant_total = montant_total;
    }






}