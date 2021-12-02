package at.fhv.sysarch.lab2.homeautomation.blackboard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;
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
    private ActorRef<Blinds.BlindsCommand> blinds;
    private Double temperature = 0.0;
    private WeatherSensor.Weather weather = WeatherSensor.Weather.SUNNY;
    private boolean movieIsPlaing = false;

    public static Behavior<Blackboard.BlackBoardCommand> create(ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Blinds.BlindsCommand> blinds){
        return Behaviors.setup(context -> new Blackboard(context, airCondition, blinds));
    }

    private Blackboard(ActorContext<BlackBoardCommand> context, ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Blinds.BlindsCommand> blinds) {
        super(context);
        this.airCondition = airCondition;
        this.blinds = blinds;
        getContext().getLog().info("Blackboard: Active");
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
        getContext().getLog().info("Blackboard: Updated movie is playing to " + this.movieIsPlaing);
        getContext().getLog().info("Blackboard: Current weather = " + this.weather.toString());
        this.blinds.tell(new Blinds.HandleStateChange(this.weather, command.isPlaying));
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateTemperature(updateTemperature command){
        temperature = command.temperature;
        getContext().getLog().info("Blackboard: updated Temperatur to " + temperature);
        this.airCondition.tell(new AirCondition.EnrichedTemperature(Optional.ofNullable(command.temperature), Optional.of("Celsius")));
        return this;
    }

    private Behavior<BlackBoardCommand> onUpdateWeather (updateWeather command){
        this.weather = command.weather;
        getContext().getLog().info("Blackboard: updated Weather to " + command.weather.toString());
        getContext().getLog().info("Blackboard: Movie is Playing = " + this.movieIsPlaing);
        this.blinds.tell(new Blinds.HandleStateChange(this.weather, this.movieIsPlaing));
        return this;
    }

    private Behavior<BlackBoardCommand> onShowStatus() {
        System.out.println("Blackboard: current temperature " + temperature);
        return this;
    }


}
