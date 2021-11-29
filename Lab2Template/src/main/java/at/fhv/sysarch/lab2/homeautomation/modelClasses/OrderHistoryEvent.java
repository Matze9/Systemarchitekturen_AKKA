package at.fhv.sysarch.lab2.homeautomation.modelClasses;


public class OrderHistoryEvent {

    private Product product;
    private String title;

    public OrderHistoryEvent(Product product, String title){
        this.product = product;
        this.title = title;
    }

    public Product getProduct() {
        return product;
    }

    public String getTitle() {
        return title;
    }
}
