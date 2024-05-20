package com.atvouzx.financetracker.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label dashboardLabel;
    @FXML
    private Label transactionsLabel;
    @FXML
    private Label reportsLabel;
    @FXML
    private Label settingsLabel;
    @FXML
    private VBox contentArea;

    @FXML
    private void initialize() {
        loadDashboard();
    }

    @FXML
    private void loadDashboard() {
        loadView("dashboard");
    }

    @FXML
    private void loadTransactions() {
        loadView("transactions");
    }

    private void loadView(String viewName) {
        try {
            VBox view = FXMLLoader.load(getClass().getResource("/com/atvouzx/financetracker/" + viewName + ".fxml"));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}