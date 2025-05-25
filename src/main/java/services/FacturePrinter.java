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

public class FacturePrinter {
    private static final String[] headers = {"Médicament", "Forme", "Qte", "Prix unit.", "Total"};
    private static final float[] columnWidths = {200f, 80f, 50f, 70f, 70f};
    private static final float MARGIN = 50;
    private static final float LOGO_WIDTH = 50;
    private static final float LOGO_HEIGHT = 50;
    private static final float BOTTOM_MARGIN = 150;

    public static void generatePDF(Facture facture, List<FactureDetails> details,
                                   String outputPath, String logoResourcePath) throws IOException {
        validateParameters(facture, details, outputPath);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float yPosition = drawHeader(document, contentStream, page, logoResourcePath);
                drawContent(document, contentStream, page, facture, details, yPosition);
            } finally {
                contentStream.close();
            }

            document.save(new File(outputPath));
        }
    }

    private static void validateParameters(Facture facture, List<FactureDetails> details, String outputPath) {
        if (facture == null || details == null || outputPath == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }
    }

    private static float drawHeader(PDDocument document, PDPageContentStream contentStream,
                                    PDPage page, String logoResourcePath) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float yPosition = page.getMediaBox().getHeight() - MARGIN;

        // Nouvelle taille du logo (augmentée)
        float newLogoWidth = 80f;  // Augmenté de 50 à 80
        float newLogoHeight = 80f; // Garder le même ratio hauteur/largeur

        // Logo centré avec nouvelle taille
        float logoX = (pageWidth - newLogoWidth) / 2;
        try (InputStream logoStream = FacturePrinter.class.getResourceAsStream(logoResourcePath)) {
            if (logoStream != null) {
                PDImageXObject logo = PDImageXObject.createFromByteArray(
                        document,
                        logoStream.readAllBytes(),
                        "logo"
                );
                contentStream.drawImage(logo, logoX, yPosition - newLogoHeight, newLogoWidth, newLogoHeight);
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement du logo: " + e.getMessage());
            // Dessiner un placeholder adapté à la nouvelle taille
            contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
            contentStream.addRect(logoX, yPosition - newLogoHeight, newLogoWidth, newLogoHeight);
            contentStream.fill();
            contentStream.setNonStrokingColor(Color.BLACK);
        }

        // Ajuster la position du texte sous le logo (plus bas à cause du logo agrandi)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("PharmaSoft Pro") / 1000 * 14;
        float textX = (pageWidth - textWidth) / 2;

        contentStream.beginText();
        contentStream.newLineAtOffset(textX, yPosition - newLogoHeight - 25); // Ajusté de -20 à -25
        contentStream.showText("PharmaSoft Pro");
        contentStream.endText();

        // Sous-titre
        textWidth = PDType1Font.HELVETICA.getStringWidth("Pharmacie de référence - N° d'agrément: PH12345") / 1000 * 10;
        textX = (pageWidth - textWidth) / 2;

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(textX, yPosition - newLogoHeight - 40); // Ajusté de -35 à -40
        contentStream.showText("Pharmacie de référence - N° d'agrément: PH12345");
        contentStream.endText();

        // Ajuster l'espacement global
        return yPosition - newLogoHeight - 90; // Augmenté pour compenser le logo plus grand
    }

    private static void drawPlaceholder(PDPageContentStream contentStream, float yPosition) throws IOException {
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(MARGIN, yPosition - LOGO_HEIGHT, LOGO_WIDTH, LOGO_HEIGHT);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);
    }

    private static void drawContent(PDDocument document, PDPageContentStream contentStream,
                                    PDPage page, Facture facture, List<FactureDetails> details,
                                    float yPosition) throws IOException {
        drawTitle(contentStream, facture, yPosition);
        yPosition -= 30;

        yPosition = drawClientInfo(contentStream, facture, yPosition);
        yPosition -= 20;

        float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        yPosition = drawTable(document, contentStream, page, details, yPosition, tableWidth);

        // Maintenant le total sera placé juste après le tableau
        drawTotal(contentStream, facture, tableWidth, yPosition);
        drawFooter(contentStream, page);
    }

    private static void drawTitle(PDPageContentStream contentStream, Facture facture,
                                  float yPosition) throws IOException {
        // Position ajustée avec plus d'espace
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition - 20); // Descendu de 20px supplémentaires
        contentStream.showText("FACTURE N°" + facture.getId_Fac());
        contentStream.endText();

        String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(facture.getDate_Fac());
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN + 400, yPosition - 20); // Descendu de 20px supplémentaires
        contentStream.showText("Date: " + dateStr);
        contentStream.endText();
    }

    private static float drawClientInfo(PDPageContentStream contentStream, Facture facture,
                                        float yPosition) throws IOException {
        // Ajout d'espace avant les infos client
        yPosition -= 15;  // Espace supplémentaire entre la facture et les infos client

        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);

        // Afficher d'abord le CIN
        contentStream.showText("CIN: " + facture.getClient().getCIN());
        contentStream.newLineAtOffset(0, -20);  // Descendre d'une ligne

        // Puis le nom et prénom
        contentStream.showText("Nom et Prénom: " + facture.getClient().getNom() + " " + facture.getClient().getPrenom());

        contentStream.endText();

        // Espace après les infos client
        return yPosition - 50;  // Réduit de 70 à 50 pour compenser l'espace ajouté avant
    }

    private static float drawTable(PDDocument document, PDPageContentStream contentStream,
                                   PDPage page, List<FactureDetails> details, float yPosition,
                                   float tableWidth) throws IOException {
        drawTableHeader(contentStream, yPosition, tableWidth);
        yPosition -= 20;

        int rowIndex = 0;

        for (FactureDetails detail : details) {
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

            if (rowIndex % 2 == 0) {
                contentStream.setNonStrokingColor(240, 240, 240);
                contentStream.addRect(MARGIN, yPosition - 15, tableWidth, 20);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0);
            }

            drawTableRow(contentStream, yPosition, tableWidth, detail);
            yPosition -= 20;
            rowIndex++;
        }

        // Retourne la position Y après la dernière ligne du tableau
        return yPosition;
    }
    private static void drawTableHeader(PDPageContentStream contentStream,
                                        float y, float tableWidth) throws IOException {
        // Fond de l'en-tête
        contentStream.setNonStrokingColor(200, 200, 200);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Bordures
        contentStream.setLineWidth(1f);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);

        // Séparateurs verticaux
        float nextX = MARGIN;
        for (int i = 0; i < columnWidths.length - 1; i++) {
            nextX += columnWidths[i];
            contentStream.moveTo(nextX, y - 15);
            contentStream.lineTo(nextX, y + 5);
        }
        contentStream.stroke();

        // Texte des en-têtes
        nextX = MARGIN;
        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(nextX + 5, y - 10);
            contentStream.showText(headers[i]);
            contentStream.endText();
            nextX += columnWidths[i];
        }
    }

    private static void drawTableRow(PDPageContentStream contentStream,
                                     float y, float tableWidth,
                                     FactureDetails detail) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(MARGIN, y - 15, tableWidth, 20);

        float nextX = MARGIN;
        for (int i = 0; i < columnWidths.length - 1; i++) {
            nextX += columnWidths[i];
            contentStream.moveTo(nextX, y - 15);
            contentStream.lineTo(nextX, y + 5);
        }
        contentStream.stroke();

        String[] rowData = {
                detail.getMedicament().getNom_Med(),
                detail.getMedicament().getForme_pharmaceutique(),
                String.valueOf(detail.getQuantite()),
                String.format("%.2f DH", detail.getPrix_unitaire()),
                String.format("%.2f DH", detail.getQuantite() * detail.getPrix_unitaire())
        };

        nextX = MARGIN;
        for (int i = 0; i < rowData.length; i++) {
            contentStream.beginText();
            if (i >= 2) { // Alignement droit pour les colonnes numériques
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

    private static void drawTotal(PDPageContentStream contentStream, Facture facture,
                                  float tableWidth, float yPosition) throws IOException {
        // Positionner le total 30 unités sous la dernière ligne du tableau
        float totalY = yPosition - 30;

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        // Aligner à droite (tableWidth - 140 pour laisser de l'espace)
        contentStream.newLineAtOffset(MARGIN + tableWidth - 140, totalY);
        contentStream.showText("TOTAL: " + String.format("%.2f DH", facture.getMontant_total()));
        contentStream.endText();
    }


    private static void drawFooter(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float yPosition = 100; // Position verticale du footer

        // Ligne de séparation centrée
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
            // Calcul de la largeur du texte pour centrage parfait
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