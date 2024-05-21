package com.atvouzx.financetracker.Controller;

import com.atvouzx.financetracker.DatabaseManager;
import com.atvouzx.financetracker.Transaction;
import com.atvouzx.financetracker.Wallet;
import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardController {

    @FXML
    private VBox walletCardContainer;

    @FXML
    private MFXPaginatedTableView<Transaction> transactionTable;

    @FXML
    private void initialize() {
        loadWallets();
        loadTransactions();
    }

    private void loadWallets() {
        List<Wallet> wallets = loadWalletsFromDatabase();
        for (Wallet wallet : wallets) {
            VBox walletCard = createWalletCard(wallet);
            walletCardContainer.getChildren().add(walletCard);
        }
    }

    private VBox createWalletCard(Wallet wallet) {
        VBox card = new VBox();
        card.getStyleClass().add("wallet-card");

        Label walletName = new Label(wallet.getName());
        walletName.getStyleClass().add("wallet-title");

        Label walletBalance = new Label("Balance: " + wallet.getBalance());
        walletBalance.getStyleClass().add("wallet-balance");

        card.getChildren().addAll(walletName, walletBalance);
        return card;
    }

    private void loadTransactions() {
        List<Transaction> transactions = loadTransactionsFromDatabase();
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);

        MFXTableColumn<Transaction> amountColumn = new MFXTableColumn<>("Amount", true, Comparator.comparing(Transaction::getAmount));
        MFXTableColumn<Transaction> categoryColumn = new MFXTableColumn<>("Category", true, Comparator.comparing(Transaction::getCategory));
        MFXTableColumn<Transaction> dateColumn = new MFXTableColumn<>("Date", true, Comparator.comparing(Transaction::getDate));
        MFXTableColumn<Transaction> notesColumn = new MFXTableColumn<>("Notes", true, Comparator.comparing(Transaction::getNotes));
        MFXTableColumn<Transaction> walletColumn = new MFXTableColumn<>("Wallet", true, Comparator.comparing(t -> t.getWallet().getName()));

        amountColumn.setRowCellFactory(t -> new MFXTableRowCell<>(Transaction::getAmount));
        categoryColumn.setRowCellFactory(t -> new MFXTableRowCell<>(Transaction::getCategory));
        dateColumn.setRowCellFactory(t -> new MFXTableRowCell<>(Transaction::getDate));
        notesColumn.setRowCellFactory(t -> new MFXTableRowCell<>(Transaction::getNotes));
        walletColumn.setRowCellFactory(t -> new MFXTableRowCell<>(tr -> tr.getWallet().getName()));

        transactionTable.getTableColumns().addAll(amountColumn, categoryColumn, dateColumn, notesColumn, walletColumn);
        transactionTable.setItems(observableTransactions);
    }

    private List<Wallet> loadWalletsFromDatabase() {
        List<Wallet> wallets = new ArrayList<>();
        String query = "SELECT id, name, balance FROM wallets";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");
                wallets.add(new Wallet(id, name, balance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallets;
    }

    private List<Transaction> loadTransactionsFromDatabase() {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT amount, category, notes, date, wallet_id FROM transactions";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");
                String notes = resultSet.getString("notes");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                int walletId = resultSet.getInt("wallet_id");

                Wallet wallet = getWalletById(walletId);
                transactions.add(new Transaction(amount, category, notes, date, wallet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    private Wallet getWalletById(int walletId) {
        // Implementasikan logika untuk mengambil data wallet berdasarkan ID dari database
        // Implementasi placeholder
        return new Wallet(walletId, "Wallet " + walletId, 0.0);
    }
}
