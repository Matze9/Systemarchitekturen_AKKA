package at.fhv.sysarch.lab2.homeautomation.devices.sensors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;

import java.util.List;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightCommand> {

    public interface FridgeWeightCommand{}

    public static Behavior<FridgeWeightCommand>create(ActorRef<FridgeWeightSensor.FridgeWeightCommand>fridgeWeightSensor, String groupId, String deviceId){
        return Behaviors.setup(context -> new FridgeWeightSensor(context, fridgeWeightSensor, groupId, deviceId));
    }

    public static final class ValidateWeight implements FridgeWeightCommand{
        private List<Product> orderedProducts;
        private Stock stock;

        public ValidateWeight(List<Product> orderedProducts, Stock stock){
            this.orderedProducts = orderedProducts;
            this.stock = stock;
        }

    }


    private final String groupId;
    private final String deviceId;
    private ActorRef<FridgeWeightSensor.FridgeWeightCommand> fridgeWeightSensor;

    public FridgeWeightSensor(ActorContext<FridgeWeightCommand>context, ActorRef<FridgeWeightSensor.FridgeWeightCommand>fridgeWeightSensor, String groupId, String deviceId){
        super(context);
        this.fridgeWeightSensor = fridgeWeightSensor;
        this.groupId = groupId;
        this.deviceId= deviceId;

        getContext().getLog().info("Weight sensor successfully started");

    }


    @Override
    public Receive<FridgeWeightCommand> createReceive() {
        return newReceiveBuilder().onMessage(ValidateWeight.class, this::onWeightValidation).onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    public Behavior<FridgeWeightSensor.FridgeWeightCommand> onWeightValidation (ValidateWeight v){

        double orderedProductsWeight = 0;
        double currentWeight;
        for (Product p : v.orderedProducts){
            orderedProductsWeight += p.getWeight();
        }

        currentWeight = orderedProductsWeight + v.stock.getCurrentWeight();

        if(currentWeight <= v.stock.getMaxWeight()){

            //TODO: send ok message to fridge
            getContext().getLog().info("Fridge weight is OK");
        }else{
            //TODO: send error message to fridge
            getContext().getLog().info("Fridge weight is not OK");
        }

        return this;
    }

    private FridgeWeightSensor onPostStop(){
        getContext().getLog().info("Fridge weight sensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
