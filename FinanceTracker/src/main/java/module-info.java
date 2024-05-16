module com.atvouzx.financetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    opens com.atvouzx.financetracker to javafx.fxml;
    exports com.atvouzx.financetracker;
}