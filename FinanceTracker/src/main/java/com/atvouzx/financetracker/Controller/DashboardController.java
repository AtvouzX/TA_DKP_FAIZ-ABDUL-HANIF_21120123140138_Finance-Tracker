package com.atvouzx.financetracker.Controller;

import com.atvouzx.financetracker.DatabaseManager;
import com.atvouzx.financetracker.Wallet;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardController {

    @FXML
    private VBox walletsContainer;

    @FXML
    private void initialize() {
        // Ambil data wallet dari database
        List<Wallet> wallets = DatabaseManager.getAllWallets();

        // Tampilkan data wallet
        for (Wallet wallet : wallets) {
            Label walletLabel = new Label(wallet.getName() + ": " + wallet.getBalance());
            walletsContainer.getChildren().add(walletLabel);
        }
    }
}
