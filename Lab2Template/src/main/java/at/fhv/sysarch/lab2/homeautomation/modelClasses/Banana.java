package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

import java.time.LocalDate;

public class Banana extends Product {

    private final double weight;
    private final double price;
    private final int storagePoints;
    private final String productName;
    private final LocalDate addedOn;

    public Banana(){
        this.weight = 0.5;
        this.price = 2.49;
        this.storagePoints = 5;
        this.productName = "Banana";
        this.addedOn = LocalDate.now();
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public int getStoragePoints() {
        return storagePoints;
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public LocalDate getAddedOn() {
        return addedOn;
    }
}