package at.fhv.sysarch.lab2.homeautomation.processors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeSpaceSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.sensors.FridgeWeightSensor;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;

import java.util.LinkedList;
import java.util.List;

public class OrderProcessor extends AbstractBehavior<OrderProcessor.OrderProcessorCommand> {



    public interface OrderProcessorCommand{}

    //response from space sensor check
    public static class SpaceValidationResponse implements OrderProcessorCommand{
        private String msg;
        private ActorRef<FridgeSpaceSensor.ValidateSpace> from;

        public SpaceValidationResponse(String msg, ActorRef<FridgeSpaceSensor.ValidateSpace> from){
            this.msg = msg;
            this.from = from;
        }

        public String getMsg() {
            return msg;
        }

        public ActorRef<FridgeSpaceSensor.ValidateSpace> getFrom() {
            return from;
        }
    }

    //response from weight validation check
    public static class WeightValidationResponse implements OrderProcessorCommand{
        private String msg;
        private ActorRef<FridgeWeightSensor.ValidateWeight> from;

        public WeightValidationResponse(String msg, ActorRef<FridgeWeightSensor.ValidateWeight> from){
            this.msg = msg;
            this.from = from;
        }

        public String getMsg() {
            return msg;
        }

        public ActorRef<FridgeWeightSensor.ValidateWeight> getFrom() {
            return from;
        }
    }

    //call for the space sensor
    public static class CallFridgeSpaceSensor implements OrderProcessorCommand{
        private LinkedList<Product> products;
        public final ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor;

        public CallFridgeSpaceSensor(LinkedList<Product> products, ActorRef<FridgeSpaceSensor.ValidateSpace> spaceSensor){
            this.products= products;
            this.spaceSensor = spaceSensor;
        }
    }

    //call for the weight sensor
    public static class CallFridgeWeightSensor implements OrderProcessorCommand{
        private LinkedList<Product> products;
        public final ActorRef<FridgeWeightSensor.ValidateWeight> weightSensor;

        public CallFridgeWeightSensor(LinkedList<Product> products, ActorRef<FridgeWeightSensor.ValidateWeight> weightSensor){
            this.products = products;
            this.weightSensor = weightSensor;
        }
    }


    //response from fridge when items are added
    public static class FridgeItemsAddedResponse implements OrderProcessorCommand{
        private final String msg;
        private final ActorRef<Fridge.FridgeCommand>from;

        public FridgeItemsAddedResponse(String msg, ActorRef<Fridge.FridgeCommand> from){
            this.msg = msg;
            this.from = from;
        }
    }





    //order processor setup
    public static Behavior<OrderProcessorCommand>create(ActorRef<Fridge.FridgeCommand>fridge, Stock stock, LinkedList<Product> orderItems){
        return Behaviors.setup(context -> new OrderProcessor(context, fridge, stock, orderItems));
    }

    private static Stock stock;
    private LinkedList<Product> orderItems;
    //private final ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessorCommand;
    private final ActorRef<Fridge.FridgeCommand> fridge;
    private String weightSensorResponse;
    private String spaceSensorResponse;
    private String fridgeItemsAddedResponse;

    public OrderProcessor(
            ActorContext<OrderProcessorCommand>context,
            ActorRef<Fridge.FridgeCommand> fridge,
            Stock stock,
            LinkedList<Product> orderItems){

        super(context);
        this.stock = stock;
        this.orderItems = orderItems;
        this.fridge = fridge;
        //this.orderProcessorCommand = orderProcessorCommand;
        this.weightSensorResponse = "ERROR";
        this.spaceSensorResponse = "ERROR";
        this.fridgeItemsAddedResponse = "ERROR";

        getContext().getLog().info("[ORDERPROCESSOR] activated");
    }


    //sends request to space sensor
    private Behavior<OrderProcessorCommand> onFridgeSpaceSensorCall(CallFridgeSpaceSensor c){
        c.spaceSensor.tell(new FridgeSpaceSensor.ValidateSpace(c.products, stock, getContext().getSelf()));
        return Behaviors.same();
    }

    //receives response from space sensor
    private Behavior<OrderProcessorCommand> onFridgeSpaceSensorResponse(SpaceValidationResponse s){
        spaceSensorResponse = s.getMsg();
        getContext().getLog().info("[ORDERPROCESSOR] response recieved from space sensor: " + s.getMsg());
        return validateSensors();
    }

    //send request to weight sensor
    private Behavior<OrderProcessorCommand> onFridgeWeightSensorCall(CallFridgeWeightSensor c){
        c.weightSensor.tell(new FridgeWeightSensor.ValidateWeight(c.products, stock, getContext().getSelf()));
        return Behaviors.same();
    }

    //receives response from weight sensor
    private Behavior<OrderProcessorCommand> onFridgeWeightSensorResponse(WeightValidationResponse w){
        weightSensorResponse = w.getMsg();
        getContext().getLog().info("[ORDERPROCESSOR] response recieved from weight sensor:  " + w.getMsg());
        return validateSensors();
    }


    //returns a ok msg from the fridge when all items are added
    private Behavior<OrderProcessorCommand> onFridgeItemsAddedResponse(FridgeItemsAddedResponse f){
        this.fridgeItemsAddedResponse = f.msg;
        getContext().getLog().info("[ORDERPROCESSOR] response from fridge: all items were added.");
        return addItemsToFridgeIfSensorResponseIsOk();

    }

    private Behavior<OrderProcessorCommand>validateSensors(){
        if(weightSensorResponse.equals("OK") && spaceSensorResponse.equals("OK")){
            getContext().getLog().info("[ORDERPROCESSOR] both sensors OK");
            fridge.tell(new Fridge.AddItems(orderItems, getContext().getSelf()));
            return addItemsToFridgeIfSensorResponseIsOk();
            //return Behaviors.stopped();
        }else{

            //TODO: proper error handling and error msg for client
            return this;
        }
    }

    private Behavior<OrderProcessorCommand> addItemsToFridgeIfSensorResponseIsOk(){

        if(fridgeItemsAddedResponse.equals("OK")){
            getContext().getLog().info("[ORDERPROCESSOR] YOUR RECEIPT: ");
            orderItems.forEach(product -> getContext().getLog().info(product.getProductName() + ", ADDED ON: " + product.getAddedOn()));
            return Behaviors.stopped();
        }
        return this;

    }


    @Override
    public Receive<OrderProcessorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(CallFridgeSpaceSensor.class, this::onFridgeSpaceSensorCall)
                .onMessage(SpaceValidationResponse.class, this::onFridgeSpaceSensorResponse)
                .onMessage(CallFridgeWeightSensor.class, this::onFridgeWeightSensorCall)
                .onMessage(WeightValidationResponse.class, this::onFridgeWeightSensorResponse)
                .onMessage(FridgeItemsAddedResponse.class, this::onFridgeItemsAddedResponse)
                .build();

    }


}
