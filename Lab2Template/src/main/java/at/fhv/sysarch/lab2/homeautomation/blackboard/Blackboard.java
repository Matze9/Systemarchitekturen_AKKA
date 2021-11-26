package at.fhv.sysarch.lab2.homeautomation.blackboard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;

import java.util.Optional;

public class Blackboard extends AbstractBehavior<Blackboard.BlackBoardCommand> {

    //////////////COMMANDS//////////////////
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

    public static class updateWeather implements BlackBoardCommand {
        public final WeatherSensor.Weather weather;

        public updateWeather (WeatherSensor.Weather newWeather){
            this.weather = newWeather;
        }
    }

    public static class startStopMovie implements BlackBoardCommand {
        public final boolean isPlaying;

        public startStopMovie (boolean isPlaying){
            this.isPlaying = isPlaying;
        }
    }

    ////////////BLACKBOARD/////////////////////

    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private Double temperature = 0.0;
    private WeatherSensor.Weather weather;
    private boolean movieIsPlaing = false;

    public static Behavior<Blackboard.BlackBoardCommand> create(ActorRef<AirCondition.AirConditionCommand> airCondition){
        return Behaviors.setup(context -> new Blackboard(context, airCondition));
    }

    private Blackboard(ActorContext<BlackBoardCommand> context, ActorRef<AirCondition.AirConditionCommand> airCondition) {
        super(context);
        this.airCondition = airCondition;
        getContext().getLog().info("Blackboard Active");
    }


    @Override
    public Receive<BlackBoardCommand> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(showStatus.INSTANCE, this::onShowStatus)
                .onMessage(updateTemperature.class, this::onUpdateTemperature)
                .onMessage(updateWeather.class, this::onUpdateWeather)
                .onMessage(startStopMovie.class, this::onStartStopMovie)
                .build();
    }

    private Behavior<BlackBoardCommand> onStartStopMovie (startStopMovie command){
        movieIsPlaing = command.isPlaying;
        getContext().getLog().info("Movie is playing = " + movieIsPlaing);
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateTemperature(updateTemperature command){
        temperature = command.temperature;
        getContext().getLog().info("Updated temperature on Blackboard to " + temperature);
        this.airCondition.tell(new AirCondition.EnrichedTemperature(Optional.ofNullable(command.temperature), Optional.of("Celsius")));
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateWeather (updateWeather command){
        this.weather = command.weather;
        getContext().getLog().info("Updated weather on Blackboard to " + command.weather.toString());
        return this;
    }

    private Behavior<BlackBoardCommand> onShowStatus() {
        System.out.println("Current Temperature: " + temperature);
        return this;
    }


}
