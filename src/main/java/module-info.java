module com.example.startupledgerpro {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // 1. Unlocks the generic JDBC database connectivity API
    requires java.sql;

    // 2. Unlocks the specific runtime code for the SQLite driver library
    requires org.xerial.sqlitejdbc;

    // 3. Open your packages so JavaFX's reflection mechanism can load your FXML files cleanly
    opens com.example.startupledgerpro to javafx.fxml;
    opens com.example.startupledgerpro.controller to javafx.fxml; // 🔓 Crucial fix for your UI controllers!
    opens com.example.startupledgerpro.service to javafx.fxml;    // 🔓 Safeguard for service references

    exports com.example.startupledgerpro;
}