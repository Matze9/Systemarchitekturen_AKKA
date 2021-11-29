package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;

import java.time.LocalDate;
import java.util.function.LongConsumer;

public class Milk extends Product {

    private final double weight;
    private final double price;
    private final int storagePoints;
    private final String productName;
    private final LocalDate addedOn;

    public Milk(){
        this.weight = 1.0;
        this.price = 1.29;
        this.storagePoints = 50;
        this.productName = "Milk";
        addedOn = LocalDate.now();
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