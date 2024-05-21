package com.atvouzx.financetracker.Controller;

import com.atvouzx.financetracker.DatabaseManager;
import com.atvouzx.financetracker.Transaction;
import com.atvouzx.financetracker.Wallet;
import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionsController {

    @FXML
    private MFXTextField amountField;

    @FXML
    private MFXFilterComboBox<String> categoryComboBox;

    @FXML
    private TextArea notesTextArea;

    @FXML
    private MFXDatePicker datePicker;

    @FXML
    private MFXFilterComboBox<Wallet> walletComboBox;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private TableColumn<Transaction, String> notesColumn;

    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;

    @FXML
    private TableColumn<Transaction, String> walletColumn;

    @FXML
    private MFXFilterComboBox<String> filterCategoryComboBox;

    @FXML
    private MFXFilterComboBox<Wallet> filterWalletComboBox;

    @FXML
    private MFXTextField filterNotesField;

    private List<Wallet> wallets;
    private ObservableList<Transaction> transactionList;

    @FXML
    private void initialize() {
        // Initialize category and wallet combo boxes
        categoryComboBox.getItems().addAll("Pendapatan", "Makanan", "Transportasi", "Hiburan", "Utilitas", "Lainnya");
        filterCategoryComboBox.getItems().addAll("Semua", "Pendapatan", "Makanan", "Transportasi", "Hiburan", "Utilitas", "Lainnya");
        filterCategoryComboBox.setValue("Semua");

        List<Wallet> wallets = DatabaseManager.getAllWallets();
        walletComboBox.getItems().addAll(wallets);

        // Initialize table columns
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        walletColumn.setCellValueFactory(new PropertyValueFactory<>("wallet"));

        // Load transactions from database
        loadTransactionsFromDatabase();

    }

    @FXML
    private void saveTransaction() {
        String amountText = amountField.getText();
        String category = categoryComboBox.getValue();
        String notes = notesTextArea.getText();
        LocalDate date = datePicker.getValue();
        Wallet wallet = walletComboBox.getValue();

        if (amountField.getText().isEmpty() || categoryComboBox.getValue() == null ||
                datePicker.getValue() == null || walletComboBox.getValue() == null) {
            showError("Silahkan isi semua bagian");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Biaya harus berupa angka");
            return;
        }



        // Periksa tipe kategori (income/expense) dan perbarui saldo dompet
        String categoryType = getCategoryType(category);
        if ("expense".equalsIgnoreCase(categoryType)) {
            amount = -amount;
        } else {
            amount = +amount;
        }

        updateWalletBalanceInDatabase(wallet);

        // Create a new Transaction object
        Transaction transaction = new Transaction(amount, category, notes, date, wallet);

        // Save the transaction to the database
        saveTransactionToDatabase(transaction);

        // Perbarui saldo dompet
        wallet.updateBalance(amount);
        updateWalletBalanceInDatabase(wallet);

        // Reload transactions from database
        loadTransactionsFromDatabase();

        // Clear the form after saving the transaction
        clearForm();

    }

    private String getCategoryType(String category) {
        String query = "SELECT type FROM categories WHERE name = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, category);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "expense"; // Default to expense if not found
    }


    private void saveTransactionToDatabase(Transaction transaction) {
        String query = "INSERT INTO transactions (amount, category, notes, date, wallet_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDouble(1, transaction.getAmount());
            statement.setString(2, transaction.getCategory());
            statement.setString(3, transaction.getNotes());
            statement.setDate(4, java.sql.Date.valueOf(transaction.getDate()));
            statement.setInt(5, transaction.getWallet().getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateWalletBalanceInDatabase(Wallet wallet) {
        String query = "UPDATE wallets SET balance = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDouble(1, wallet.getBalance());
            statement.setInt(2, wallet.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactionsFromDatabase() {
        transactionList = FXCollections.observableArrayList();

        String query = "SELECT t.amount, t.category, t.notes, t.date, w.id as wallet_id, w.name as wallet_name " +
                "FROM transactions t JOIN wallets w ON t.wallet_id = w.id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");
                String notes = resultSet.getString("notes");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                Wallet wallet = new Wallet(resultSet.getInt("wallet_id"), resultSet.getString("wallet_name"), 0);

                transactionList.add(new Transaction(amount, category, notes, date, wallet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        transactionsTable.setItems(transactionList);
    }

    @FXML
    private void applyFilters() {
        String category = filterCategoryComboBox.getValue();
        Wallet wallet = filterWalletComboBox.getValue();
        String notes = filterNotesField.getText();

        ObservableList<Transaction> filteredList = FXCollections.observableArrayList(transactionList);

        if (!"All".equals(category)) {
            filteredList.removeIf(transaction -> !transaction.getCategory().equals(category));
        }

        if (wallet != null && wallet.getId() != 0) {
            filteredList.removeIf(transaction -> transaction.getWallet().getId() != wallet.getId());
        }

        if (notes != null && !notes.isEmpty()) {
            filteredList.removeIf(transaction -> !transaction.getNotes().toLowerCase().contains(notes.toLowerCase()));
        }

        transactionsTable.setItems(filteredList);
    }

    @FXML
    private void clearFilters() {
        filterCategoryComboBox.setValue("All");
        filterWalletComboBox.setValue(filterWalletComboBox.getItems().get(0));
        filterNotesField.clear();

        transactionsTable.setItems(transactionList);
    }

    private void clearForm() {
        amountField.clear();
        categoryComboBox.setValue(null);
        notesTextArea.clear();
        datePicker.setValue(null);
        walletComboBox.setValue(null);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


}

