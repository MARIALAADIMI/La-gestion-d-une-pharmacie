module ProjetJAVAFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires commons.logging;
    opens controllers to javafx.fxml;
    exports controllers;
    exports entities;
    exports dao;
    exports services;
    exports presentation;

}