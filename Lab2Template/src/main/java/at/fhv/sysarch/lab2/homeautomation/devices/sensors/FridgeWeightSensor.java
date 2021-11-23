package at.fhv.sysarch.lab2.homeautomation.devices.sensors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.Fridge;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Product;
import at.fhv.sysarch.lab2.homeautomation.modelClasses.Stock;
import at.fhv.sysarch.lab2.homeautomation.processors.OrderProcessor;

import java.util.List;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.ValidateWeight> {


    public static class ValidateWeight{
        private List<Product> orderedProducts;
        private Stock stock;
        public final ActorRef<OrderProcessor.OrderProcessorCommand> replyTo;

        public ValidateWeight(List<Product> orderedProducts, Stock stock, ActorRef<OrderProcessor.OrderProcessorCommand> replyTo){
            this.orderedProducts = orderedProducts;
            this.stock = stock;
            this.replyTo = replyTo;
        }
    }

    public static Behavior<ValidateWeight>create(){
        return Behaviors.setup(FridgeWeightSensor::new);
    }

    public FridgeWeightSensor(ActorContext<ValidateWeight> context){
        super(context);
        getContext().getLog().info("WEIGHT SENSOR ACTIVE.");

    }

    private Behavior<FridgeWeightSensor.ValidateWeight> onWeightValidation(ValidateWeight v){
        double orderedProductsWeight = 0;
        double currentWeight;
        for (Product p : v.orderedProducts){
            orderedProductsWeight += p.getWeight();
        }

        currentWeight = orderedProductsWeight + v.stock.getCurrentWeight();

        if(currentWeight <= v.stock.getMaxWeight()){

            v.replyTo.tell(new OrderProcessor.WeightValidationResponse("OK", getContext().getSelf()));
            getContext().getLog().info("Fridge weight is OK");
        }else{
            v.replyTo.tell(new OrderProcessor.WeightValidationResponse("ERROR", getContext().getSelf()));
            getContext().getLog().info("Fridge weight ERROR");
        }

        return this;
    }



    @Override
    public Receive<ValidateWeight> createReceive() {
        return newReceiveBuilder().onMessage(ValidateWeight.class, this::onWeightValidation).build();
    }
}
