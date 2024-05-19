package com.atvouzx.financetracker.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class SidebarController {

    @FXML
    private ListView<String> menuList;

    private MainWindowController mainWindowController;

    @FXML
    private void initialize() {
        menuList.getItems().addAll("Dashboard", "Transactions", "Reports", "Settings");
    }

    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
        setMenuListeners();
    }

    private void setMenuListeners() {
        menuList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (newValue) {
                    case "Dashboard":
                        mainWindowController.loadView("dashboard");
                        break;
                    case "Transactions":
                        mainWindowController.loadView("transactions");
                        break;
                    case "Reports":
                        // Load Reports
                        break;
                    case "Settings":
                        // Load Settings
                        break;
                }
            }
        });
    }
}