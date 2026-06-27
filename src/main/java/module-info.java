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

    // 3. Open your core packages so JavaFX's reflection mechanism can load your FXML files
    opens com.example.startupledgerpro to javafx.fxml;

    // 4. Critical for Days 2 & 3: Opens your UI controllers to JavaFX
    // (Assuming you'll place controllers inside a 'controller' or sub-packages later)
    // For now, opening the root package takes care of your main application boot.

    exports com.example.startupledgerpro;
}