package com.atvouzx.financetracker.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane rootPane;

    private SidebarController sidebarController;

    @FXML
    private void initialize() {
        loadSidebar();
        loadView("dashboard");
    }

    public void loadView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/atvouzx/financetracker/" + viewName + ".fxml"));
            rootPane.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/atvouzx/financetracker/sidebar.fxml"));
            rootPane.setLeft(loader.load());
            sidebarController = loader.getController();
            sidebarController.setMainWindowController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}