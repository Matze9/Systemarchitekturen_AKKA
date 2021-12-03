package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.blackboard.Blackboard;

import java.util.Optional;

public class TemperatureSensor extends AbstractBehavior<TemperatureSensor.TemperatureCommand> {

    ////////////TODO:COMMANDS///////////////////

    public interface TemperatureCommand {}

    public static final class ReadTemperature implements TemperatureCommand {
        final Optional<Double> value;

        public ReadTemperature(Optional<Double> value) {
            this.value = value;
        }
    }

    ////////////TODO:TEMPERATURE SENSOR/////////////


    private final String groupId;
    private final String deviceId;

    private ActorRef<Blackboard.BlackBoardCommand> blackBoard;

    public static Behavior<TemperatureCommand> create( ActorRef<Blackboard.BlackBoardCommand> blackBoard, String groupId, String deviceId) {
        return Behaviors.setup(context -> new TemperatureSensor(context, blackBoard, groupId, deviceId));
    }

    public TemperatureSensor(ActorContext<TemperatureCommand> context, ActorRef<Blackboard.BlackBoardCommand> blackBoard, String groupId, String deviceId) {
        super(context);
        this.blackBoard = blackBoard;
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("TemperatureSensor started");
    }

    @Override
    public Receive<TemperatureCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<TemperatureCommand> onReadTemperature(ReadTemperature r) {
        getContext().getLog().info("TemperatureSensor received {}", r.value.get());
        //this.airCondition.tell(new AirCondition.EnrichedTemperature(r.value, Optional.of("Celsius")));

        this.blackBoard.tell(new Blackboard.updateTemperature(r.value.get()));
        return this;
    }

    private TemperatureSensor onPostStop() {
        getContext().getLog().info("TemperatureSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}
