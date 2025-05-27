package services;

import entities.Facture;
import entities.FactureDetails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Service de génération de factures au format PDF
 */
public class FacturePrinter {
    // Configuration des constantes pour la mise en page
    private static final String[] headers = {"Médicament", "Forme", "Qte", "Prix unit.", "Total"};
    private static final float[] columnWidths = {200f, 80f, 50f, 70f, 70f};
    private static final float MARGIN = 50; // Marge générale du document
    private static final float LOGO_WIDTH = 50; // Largeur par défaut du logo
    private static final float LOGO_HEIGHT = 50; // Hauteur par défaut du logo
    private static final float BOTTOM_MARGIN = 150; // Marge inférieure pour éviter le débordement

    //Génère un PDF de facture
    public static void generatePDF(Facture facture, List<FactureDetails> details,
                                   String outputPath, String logoResourcePath) throws IOException {
        validateParameters(facture, details, outputPath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Création du flux de contenu pour dessiner sur la page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = drawHeader(document, contentStream, page, logoResourcePath);
                drawContent(document, contentStream, page, facture, details, yPosition);
            } finally {
                contentStream.close();
            }

            // Sauvegarde finale du document
            document.save(new File(outputPath));
        }
    }

    //Validation des paramètres d'entrée
    private static void validateParameters(Facture facture, List<FactureDetails> details, String outputPath) {
        if (facture == null || details == null || outputPath == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }
    }

    //Dessine l'en-tête du document avec le logo et les informations de la pharmacie
    private static float drawHeader(PDDocument document, PDPageContentStream contentStream,
                                    PDPage page, String logoResourcePath) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float yPosition = page.getMediaBox().getHeight() - MARGIN;

        // Configuration de la taille du logo (augmentée pour plus de visibilité)
        float newLogoWidth = 80f;
        float newLogoHeight = 80f;

        // Positionnement centré du logo
        float logoX = (pageWidth - newLogoWidth) / 2;

        try (InputStream logoStream = FacturePrinter.class.getResourceAsStream(logoResourcePath)) {
            if (logoStream != null) {
                // Chargement et dessin du logo
                PDImageXObject logo = PDImageXObject.createFromByteArray(
                        document,
                        logoStream.readAllBytes(),
                        "logo"
                );
                contentStream.drawImage(logo, logoX, yPosition - newLogoHeight, newLogoWidth, newLogoHeight);
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement du logo: " + e.getMessage());
            // Fallback: dessin d'un placeholder si le logo n'est pas disponible
            drawPlaceholder(contentStream, yPosition);
        }

        // Texte sous le logo - Nom de la pharmacie
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("PharmaSoft Pro") / 1000 * 14;
        float textX = (pageWidth - textWidth) / 2;

        contentStream.beginText();
        contentStream.newLineAtOffset(textX, yPosition - newLogoHeight - 25);
        contentStream.showText("PharmaSoft Pro");
        contentStream.endText();

        // Sous-titre - Informations d'agrément
        textWidth = PDType1Font.HELVETICA.getStringWidth("Pharmacie de référence - N° d'agrément: PH12345") / 1000 * 10;
        textX = (pageWidth - textWidth) / 2;

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(textX, yPosition - newLogoHeight - 40);
        contentStream.showText("Pharmacie de référence - N° d'agrément: PH12345");
        contentStream.endText();

        // Retourne la nouvelle position Y après l'en-tête
        return yPosition - newLogoHeight - 90;
    }

    //Dessine un placeholder si le logo n'est pas disponible
    private static void drawPlaceholder(PDPageContentStream contentStream, float yPosition) throws IOException {
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(MARGIN, yPosition - LOGO_HEIGHT, LOGO_WIDTH, LOGO_HEIGHT);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);
    }

    //Dessine le contenu principal de la facture
    private static void drawContent(PDDocument document, PDPageContentStream contentStream,
                                    PDPage page, Facture facture, List<FactureDetails> details,
                                    float yPosition) throws IOException {
        // 1. Titre de la facture avec numéro et date
        drawTitle(contentStream, facture, yPosition);
        yPosition -= 30;

        // 2. Informations du client
        yPosition = drawClientInfo(contentStream, facture, yPosition);
        yPosition -= 20;

        // 3. Tableau des articles
        float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        yPosition = drawTable(document, contentStream, page, details, yPosition, tableWidth);

        // 4. Total de la facture
        drawTotal(contentStream, facture, tableWidth, yPosition);

        // 5. Pied de page
        drawFooter(contentStream, page);
    }

    //Dessine le titre de la facture avec numéro et date
    private static void drawTitle(PDPageContentStream contentStream, Facture facture,
                                  float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        // Numéro de facture à gauche
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition - 20);
        contentStream.showText("FACTURE N°" + facture.getId_Fac());
        contentStream.endText();

        // Date à droite
        String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(facture.getDate_Fac());
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 400, yPosition - 20);
        contentStream.showText("Date: " + dateStr);
        contentStream.endText();
    }

    //Dessine les informations du client
    private static float drawClientInfo(PDPageContentStream contentStream, Facture facture,
                                        float yPosition) throws IOException {
        yPosition -= 15; // Espace supplémentaire avant les infos

        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);

        // Affichage du CIN
        contentStream.showText("CIN: " + facture.getClient().getCIN());
        contentStream.newLineAtOffset(0, -20);

        // Affichage du nom et prénom
        contentStream.showText("Nom et Prénom: " + facture.getClient().getNom() + " " + facture.getClient().getPrenom());
        contentStream.endText();

        return yPosition - 50;
    }

    //Dessine le tableau des articles de la facture
    private static float drawTable(PDDocument document, PDPageContentStream contentStream,
                                   PDPage page, List<FactureDetails> details, float yPosition,
                                   float tableWidth) throws IOException {
        // En-tête du tableau
        drawTableHeader(contentStream, yPosition, tableWidth);
        yPosition -= 20;

        int rowIndex = 0;

        for (FactureDetails detail : details) {
            // Gestion de la pagination si on arrive en bas de page
            if (yPosition < BOTTOM_MARGIN) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);

                yPosition = page.getMediaBox().getHeight() - MARGIN;
                drawTableHeader(contentStream, yPosition, tableWidth);
                yPosition -= 20;
                rowIndex = 0;
            }

            // Alternance des couleurs de fond pour les lignes
            if (rowIndex % 2 == 0) {
                contentStream.setNonStrokingColor(240, 240, 240);
                contentStream.addRect(MARGIN, yPosition - 15, tableWidth, 20);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0);
            }

            // Dessin de la ligne
            drawTableRow(contentStream, yPosition, tableWidth, detail);
            yPosition -= 20;
            rowIndex++;
        }

        return yPosition;
    }

    //Dessine l'en-tête du tableau
    private static void drawTableHeader(PDPageContentStream contentStream,
                                        float y, float tableWidth) throws IOException {
        // Fond gris pour l'en-tête
        contentStream.setNonStrokingColor(200, 200, 200);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Bordures du tableau
        contentStream.setLineWidth(1f);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);

        // Séparateurs verticaux entre colonnes
        float nextX = MARGIN;
        for (int i = 0; i < columnWidths.length - 1; i++) {
            nextX += columnWidths[i];
            contentStream.moveTo(nextX, y - 15);
            contentStream.lineTo(nextX, y + 5);
        }
        contentStream.stroke();

        // Texte des en-têtes de colonnes
        nextX = MARGIN;
        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(nextX + 5, y - 10);
            contentStream.showText(headers[i]);
            contentStream.endText();
            nextX += columnWidths[i];
        }
    }

    //Dessine une ligne du tableau
    private static void drawTableRow(PDPageContentStream contentStream,
                                     float y, float tableWidth,
                                     FactureDetails detail) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);

        // Séparateurs verticaux
        float nextX = MARGIN;
        for (int i = 0; i < columnWidths.length - 1; i++) {
            nextX += columnWidths[i];
            contentStream.moveTo(nextX, y - 15);
            contentStream.lineTo(nextX, y + 5);
        }
        contentStream.stroke();

        // Données de la ligne
        String[] rowData = {
                detail.getMedicament().getNom_Med(),
                detail.getMedicament().getForme_pharmaceutique(),
                String.valueOf(detail.getQuantite()),
                String.format("%.2f DH", detail.getPrix_unitaire()),
                String.format("%.2f DH", detail.getQuantite() * detail.getPrix_unitaire())
        };

        // Placement du texte dans chaque colonne
        nextX = MARGIN;
        for (int i = 0; i < rowData.length; i++) {
            contentStream.beginText();
            if (i >= 2) { // Alignement centré pour les colonnes numériques
                float textWidth = PDType1Font.HELVETICA.getStringWidth(rowData[i]) / 1000 * 10;
                contentStream.newLineAtOffset(nextX + (columnWidths[i] - textWidth) / 2, y - 10);
            } else {
                contentStream.newLineAtOffset(nextX + 5, y - 10);
            }
            contentStream.showText(rowData[i]);
            contentStream.endText();
            nextX += columnWidths[i];
        }
    }

    //Dessine le montant total de la facture
    private static void drawTotal(PDPageContentStream contentStream, Facture facture,
                                  float tableWidth, float yPosition) throws IOException {
        float totalY = yPosition - 30; // Position sous le tableau

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        // Alignement à droite
        contentStream.newLineAtOffset(MARGIN + tableWidth - 140, totalY);
        contentStream.showText("TOTAL: " + String.format("%.2f DH", facture.getMontant_total()));
        contentStream.endText();
    }

    //Dessine le pied de page avec les informations de contact
    private static void drawFooter(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float yPosition = 100; // Position fixe en bas de page

        // Ligne de séparation
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();
        yPosition -= 20;

        // Informations de contact centrées
        String[] footerLines = {
                "85 Riad Oulad Mna avenue Sahl Rhone, TEMARA | Tél: 0537 718 718 | Fax: 0556453423",
                "Email: Contact@PharmaSoftPro.ma | IF: 18823456 | Patente: 456734 | ICE: 003423456787"
        };

        contentStream.setFont(PDType1Font.HELVETICA, 8);

        for (String line : footerLines) {
            // Calcul de la largeur pour centrage parfait
            float textWidth = PDType1Font.HELVETICA.getStringWidth(line) / 1000 * 8;
            float startX = (pageWidth - textWidth) / 2;

            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= 12;
        }
    }
}