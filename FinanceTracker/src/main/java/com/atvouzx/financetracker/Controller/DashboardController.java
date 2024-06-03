package com.atvouzx.financetracker.Controller;

import com.atvouzx.financetracker.DatabaseManager;
import com.atvouzx.financetracker.Transaction;
import com.atvouzx.financetracker.Wallet;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.DoubleFilter;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.LongFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardController {
    private ObservableList<Transaction> transactions;

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
        transactions = FXCollections.observableArrayList();
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions")) {

            while (resultSet.next()) {

                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");
                String notes = resultSet.getString("notes");
                long epochMilli = resultSet.getLong("date"); // Ambil nilai epoch dari result set
                LocalDate date = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate(); // Konversi epoch ke LocalDate
                int walletId = resultSet.getInt("wallet_id");
                Wallet wallet = getWalletById(walletId);

                if (wallet != null) {
                    Transaction transaction = new Transaction( amount, category, notes, date, wallet);
                    transactions.add(transaction);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        transactionTable.getFilters().addAll(
                new DoubleFilter<>("Amount", Transaction::getAmount),
                new StringFilter<>("Category", Transaction::getCategory),
                new StringFilter<>("Notes", Transaction::getNotes)
        );
        transactionTable.setItems(transactions);
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
                int id =resultSet.getInt("id");
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
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM wallets WHERE id = ?")) {

            statement.setInt(1, walletId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");
                return new Wallet(walletId, name, balance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // or handle this case as you see fit
    }

    private void deleteTransactionFromDatabase(int transactionId) {
        String query = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transactionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToExcel() {
        // Membuat FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        // Menampilkan dialog penyimpanan file
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Transactions");

            // Membuat header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Amount", "Category", "Notes", "Date", "Wallet"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Mengisi data transaksi
            List<Transaction> transactions = transactionTable.getItems();
            for (int i = 0; i < transactions.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Transaction transaction = transactions.get(i);

                row.createCell(0).setCellValue(transaction.getAmount());
                row.createCell(1).setCellValue(transaction.getCategory());
                row.createCell(2).setCellValue(transaction.getNotes());
                row.createCell(3).setCellValue(transaction.getDate().toString());
                row.createCell(4).setCellValue(transaction.getWallet().getName());
            }

            // Menyimpan file Excel
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

