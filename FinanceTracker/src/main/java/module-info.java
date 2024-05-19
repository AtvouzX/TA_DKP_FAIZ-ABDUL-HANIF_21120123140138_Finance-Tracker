module com.atvouzx.financetracker {
    
    requires MaterialFX;
    requires java.sql;


    opens com.atvouzx.financetracker to javafx.fxml;
    exports com.atvouzx.financetracker;
    exports com.atvouzx.financetracker.Controller;
    opens com.atvouzx.financetracker.Controller to javafx.fxml;
}