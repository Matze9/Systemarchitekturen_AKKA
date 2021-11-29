package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

import java.time.LocalDate;

public class Butter extends Product {

    private final double weight;
    private final double price;
    private final int storagePoints;
    private final String productName;
    private LocalDate addedOn;

    public Butter(){
        this.weight = 0.25;
        this.price = 1.69;
        this.storagePoints = 2;
        this.productName = "Butter";
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