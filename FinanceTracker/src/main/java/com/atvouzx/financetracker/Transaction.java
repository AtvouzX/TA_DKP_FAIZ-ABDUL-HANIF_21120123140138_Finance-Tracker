package com.atvouzx.financetracker;

import java.time.LocalDate;

public class Transaction {
    private double amount;
    private String category;
    private String notes;
    private LocalDate date;
    private Wallet wallet;

    public Transaction(double amount, String category, String notes, LocalDate date, Wallet wallet) {
        this.amount = amount;
        this.category = category;
        this.notes = notes;
        this.date = date;
        this.wallet = wallet;
    }

    // Getter and setter methods for all properties
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

}
