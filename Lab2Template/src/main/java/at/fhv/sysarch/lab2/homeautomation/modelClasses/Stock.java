package at.fhv.sysarch.lab2.homeautomation.modelClasses;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Stock {


    private HashMap<String, LinkedList<Product>> allProducts;
    private final double maxWeight;
    private final double currentWeight;
    private final int maxSpace;
    private final int currentSpace;

    public Stock(){
        allProducts = new HashMap<>();
        maxWeight = 50.0;
        currentWeight = 0.0;
        maxSpace = 100;
        currentSpace = 0;
    }

    public void addProduct (Product product){
        //TODO: throw exceptions
        if(allProducts.containsKey(product.getProductName())){
            allProducts.get(product.getProductName()).add(product);
        }else{
            LinkedList<Product> newFoodEntry = new LinkedList<>();
            newFoodEntry.add(product);
            allProducts.put(product.getProductName(), newFoodEntry);
        }
    }

    public void removeProduct (Product product){
            allProducts.get(product.getProductName()).removeFirst();
    }



    public HashMap<String, LinkedList<Product>> getAllProducts() {
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
