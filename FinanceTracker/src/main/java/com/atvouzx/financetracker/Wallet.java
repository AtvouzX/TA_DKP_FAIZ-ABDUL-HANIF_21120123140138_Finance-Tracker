package com.atvouzx.financetracker;

public class Wallet {
    private int id;
    private String name;
    private double balance;

    public Wallet(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return name; // This is important for displaying in ComboBox
    }
}
