package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import java.util.LinkedList;
import java.util.List;

public class Stock {

    private List<Product> allProducts;
    private final double maxWeight;
    private final double currentWeight;
    private final int maxSpace;
    private final int currentSpace;

    public Stock(){
        allProducts = new LinkedList<>();
        maxWeight = 50.0;
        currentWeight = 0.0;
        maxSpace = 100;
        currentSpace = 0;
    }

    public void addProduct (Product product){
        //TODO: throw exceptions
        allProducts.add(product);
    }

    public void removeProduct (Product product){
        allProducts.remove(product);
    }



    public List<Product> getAllProducts() {
        return allProducts;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public int getMaxSpace() {
        return maxSpace;
    }

    public int getCurrentSpace() {
        return currentSpace;
    }
}
