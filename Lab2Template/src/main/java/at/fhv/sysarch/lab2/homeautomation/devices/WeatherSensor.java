package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.blackboard.Blackboard;

public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherCommand> {

    //////////////COMMANDS//////////////////
    public interface WeatherCommand {}

    public static class updateWeather implements WeatherSensor.WeatherCommand {
        public final Weather weather;
        public updateWeather(Weather newWeather){
            this.weather = newWeather;
        }
    }

    public enum Weather {
        SUNNY,
        CLOUDY
    }

    ////////////WEATHERSENSOR/////////////////////

    private ActorRef<Blackboard.BlackBoardCommand> blackboard;

    public static Behavior<WeatherSensor.WeatherCommand> create(ActorRef<Blackboard.BlackBoardCommand> blackboard){
        return Behaviors.setup(context -> new WeatherSensor(context, blackboard));
    }

    public WeatherSensor(ActorContext<WeatherCommand> context, ActorRef<Blackboard.BlackBoardCommand> blackboard) {
        super(context);
        this.blackboard = blackboard;
        getContext().getLog().info("WeatherSensor Active");
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(updateWeather.class, this::onUpdateWeather)
                .build();
    }

    private Behavior<WeatherCommand> onUpdateWeather (updateWeather weather){
        getContext().getLog().info("WeatherSensor received {} ", weather.weather.toString());
        this.blackboard.tell(new Blackboard.updateWeather(weather.weather));
        return this;
    }


}
