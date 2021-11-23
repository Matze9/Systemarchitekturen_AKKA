package at.fhv.sysarch.lab2.homeautomation.blackboard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;

import java.util.Optional;

public class Blackboard extends AbstractBehavior<Blackboard.BlackBoardCommand> {

    public interface BlackBoardCommand {}

    public enum showStatus implements BlackBoardCommand {
        INSTANCE
    }

    public static class updateTemperature implements BlackBoardCommand {
        public final Double temperature;

        public updateTemperature (Double newTemperature){
            this.temperature = newTemperature;
        }
    }

    ActorRef<AirCondition.AirConditionCommand> airCondition;

    private Double temperature = 0.0;

    public static Behavior<Blackboard.BlackBoardCommand> create(ActorRef<AirCondition.AirConditionCommand> airCondition){
        return Behaviors.setup(context -> new Blackboard(context, airCondition));
    }

    private Blackboard(ActorContext<BlackBoardCommand> context, ActorRef<AirCondition.AirConditionCommand> airCondition) {
        super(context);
        this.airCondition = airCondition;
        getContext().getLog().info("Blackboard Active");
        System.out.println("Blackboard Active");
    }


    @Override
    public Receive<BlackBoardCommand> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(showStatus.INSTANCE, this::onShowStatus)
                .onMessage(updateTemperature.class, this::onUpdateTemperature)
                .build();
    }

    private Behavior<BlackBoardCommand> onUpdateTemperature(updateTemperature command){
        temperature = command.temperature;
        getContext().getLog().info("Updated temperature on Blackboard to " + temperature);
        this.airCondition.tell(new AirCondition.EnrichedTemperature(Optional.ofNullable(command.temperature), Optional.of("Celsius")));
        return this;
    }

    private Behavior<BlackBoardCommand> onShowStatus() {
        System.out.println("Current Temperature: " + temperature);
        return this;
    }


}
