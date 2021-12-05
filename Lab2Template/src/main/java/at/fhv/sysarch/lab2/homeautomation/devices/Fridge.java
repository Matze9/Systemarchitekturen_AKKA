package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.Actor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeWeightSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.OrderHistoryEvent;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;
import scala.concurrent.impl.Promise;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {



    public interface FridgeCommand {}

    //places an order and adds products to the fridge if possible
    public static final class PlaceOrder implements FridgeCommand{
        public final LinkedList<Product> orderItems;
        public final Stock stock;

        public PlaceOrder( LinkedList<Product> orderItems, Stock stock){
            this.orderItems = orderItems;
            this.stock = stock;
        }
    }

    //get all products from the fridge
    public static final class GetProducts implements FridgeCommand{
        private HashMap<String, LinkedList<Product>> products;

        public GetProducts (){products = stock.getAllProducts();}
    }

    //removes item from fridge. TODO: place order if item runs empty
    public static final class ConsumeProduct implements FridgeCommand {
        private Product product;
        public ConsumeProduct(Product product){
            this.product = product;
        }
    }

    //adds new items to the fridge
    public static final class AddItems implements FridgeCommand{
        private LinkedList<Product> products;
        private ActorRef<OrderProcessor.OrderProcessorCommand> replyTo;

        public AddItems(LinkedList<Product> products, ActorRef<OrderProcessor.OrderProcessorCommand> replyTo){
            this.products = products;
            this.replyTo = replyTo;

        }
    }
    //returns order history from Fridge
    public static final class GetItemsFromOrderHistory implements FridgeCommand {
         //TODO: send response to UI
        private LinkedList<OrderHistoryEvent> orderHistoryEvents;
        public GetItemsFromOrderHistory(){
            this.orderHistoryEvents = orderHistory;
        }

    }

    //returns available Space from fridge
    public static final class GetFridgeSpaceLeft implements FridgeCommand{
        private Stock stock1;
        public GetFridgeSpaceLeft(){
            this.stock1 = stock;
        }
    }

    //returns available Weight from fridge
    public static final class GetFridgeWeightLeft implements FridgeCommand{
        private Stock stock1;
        public GetFridgeWeightLeft(){
            this.stock1 = stock;
        }
    }





    //fridge setup
    public static Behavior<FridgeCommand> create ( Stock stock, String groupId, String deviceId){
        return Behaviors.setup(context -> new Fridge(context, stock, groupId, deviceId));
    }

    private final String groupId;
    private final String deviceId;
    private ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor;
    private ActorRef<FridgeWeightSensor.ValidateWeight> weightSensor;
    private ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessor;
    private static Stock stock;
    private static LinkedList<OrderHistoryEvent>orderHistory;


    public Fridge(ActorContext<FridgeCommand> context, Stock stock, String groupId, String deviceId){
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.stock = stock;
        orderHistory = new LinkedList<>();

        this.spaceSensor = context.spawn(FridgeSpaceSensor.create(), "space-sensor");
        this.weightSensor = context.spawn(FridgeWeightSensor.create(), "weight-sensor");
        getContext().getLog().info("Fridge successfully started");
    }



    //behaviours
    private Behavior<FridgeCommand> onOrderPlaced(PlaceOrder p){
        orderProcessor = getContext().spawn(OrderProcessor.create(getContext().getSelf(), stock, p.orderItems), "processor");
        orderProcessor.tell(new OrderProcessor.CallFridgeWeightSensor(p.orderItems, weightSensor));
        orderProcessor.tell(new OrderProcessor.CallFridgeSpaceSensor(p.orderItems, spaceSensor));
        return this;
    }

    private Behavior<FridgeCommand> onItemsAdd(AddItems a){

        a.products.forEach(product -> {
            String key = product.getProductName();
            if(stock.getAllProducts().containsKey(key)){
                stock.getAllProducts().get(key).add(product);
            }else{
                LinkedList<Product> newProductList = new LinkedList<>();
                newProductList.add(product);
                stock.getAllProducts().put(key, newProductList);
            }
            orderHistory.add(new OrderHistoryEvent(product, "ADDED"));
        });
        a.replyTo.tell(new OrderProcessor.FridgeItemsAddedResponse("OK", getContext().getSelf()));

        return this;
    }


    private Behavior<FridgeCommand> onGetProducts (GetProducts g){
        getContext().getLog().info("[FRIDGE] All items in your fridge: ");
        stock.getAllProducts().forEach((productName, productList) ->{
            productList.forEach(product -> getContext().getLog().info(product.getProductName() + ", added: " + product.getAddedOn() ));
        });
        return this;
    }


    private Behavior<FridgeCommand> onProductConsumption(ConsumeProduct c){

        if(stock.getAllProducts().containsKey(c.product.getProductName())){
           stock.removeProduct(c.product);
           getContext().getLog().info("[FRIDGE] Sucessfully consumed " + c.product.getProductName(), c.product.getProductName());
           orderHistory.add(new OrderHistoryEvent(c.product, "CONSUMED"));

           //if this was the last item, automatically reorder a new one
           if(stock.getAllProducts().get(c.product.getProductName()).isEmpty()){
               getContext().getLog().info("[FRIDGE] last " + c.product.getProductName() + " consumed, initialize reorder...");

               //creating a linkedList for matching parameter
               LinkedList<Product> productToRefill = new LinkedList<>();
               productToRefill.add(c.product);
               getContext().getSelf().tell(new PlaceOrder(productToRefill, stock));
           }
        }else{
            getContext().getLog().info("[FRIDGE] You tried to remove a product which seems to be not in the fridge...");
       }
        return this;
    }

    private Behavior<FridgeCommand> onFridgeOrderHistoryRequest(GetItemsFromOrderHistory g){
        if(g.orderHistoryEvents.isEmpty()){
            getContext().getLog().info("[FRIDGE] no items added to order history");

        }else{
            getContext().getLog().info("[FRIDGE] order history: ");
            g.orderHistoryEvents.forEach(orderHistoryEvent ->
                    getContext().getLog().info(
                            orderHistoryEvent.getTitle() +
                                    ": " +
                                    orderHistoryEvent.getProduct().getProductName() +
                                    " on " +
                                    orderHistoryEvent.getProduct().getAddedOn()));
        }
       return this;
    }

    private Behavior<FridgeCommand>onSpaceLeftRequest(GetFridgeSpaceLeft g){
        int spaceLeft = g.stock1.getMaxSpace() - g.stock1.getCurrentSpace();
        getContext().getLog().info("[FRIDGE] " + spaceLeft + "/" + g.stock1.getMaxSpace() + " space left");

        return this;
    }

    private Behavior<FridgeCommand>onWeightLeftRequest(GetFridgeWeightLeft g){
        double weightLeft = g.stock1.getMaxWeight() - g.stock1.getCurrentWeight();
        getContext().getLog().info("[FRIDGE] " + weightLeft + "/" + g.stock1.getMaxWeight() + " weight left");

        return this;
    }





    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConsumeProduct.class, this::onProductConsumption)
                .onMessage(PlaceOrder.class, this::onOrderPlaced)
                .onMessage(AddItems.class, this::onItemsAdd)
                .onMessage(GetProducts.class, this::onGetProducts)
                .onMessage(GetItemsFromOrderHistory.class, this::onFridgeOrderHistoryRequest)
                .onMessage(GetFridgeSpaceLeft.class, this::onSpaceLeftRequest)
                .onMessage(GetFridgeWeightLeft.class, this::onWeightLeftRequest)
                .build();
    }


    private Fridge onPostStop(){
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
