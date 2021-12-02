package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.blackboard.Blackboard;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    ////////COMMANDS//////////

    public interface BlindsCommand {}

    public static final class HandleStateChange implements BlindsCommand {
        WeatherSensor.Weather weather;
        boolean movieIsPlaying;

        public HandleStateChange(WeatherSensor.Weather weather, boolean movieIsPlaying){
            this.weather = weather;
            this.movieIsPlaying = movieIsPlaying;
        }
    }

    public enum BlindState {
        OPEN,
        CLOSED
    }

    ////////BLINDS///////////

    private BlindState blindState = BlindState.CLOSED;

    public static Behavior<Blinds.BlindsCommand> create(){
        return Behaviors.setup(context -> new Blinds(context));
    }

    private Blinds (ActorContext<Blinds.BlindsCommand> context){
        super (context);
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Blinds.HandleStateChange.class, this::onHandleStateChange)
                .build();
    }

    private  Behavior<BlindsCommand> onHandleStateChange(Blinds.HandleStateChange command) {
        if (!command.movieIsPlaying && command.weather == WeatherSensor.Weather.CLOUDY){
            this.blindState = BlindState.OPEN;
            getContext().getLog().info("Blinds: Blinds are "  +this.blindState.toString());
        } else if (command.weather == WeatherSensor.Weather.SUNNY || command.movieIsPlaying) {
            this.blindState = BlindState.CLOSED;
            getContext().getLog().info("Blinds: Blinds are "  +this.blindState.toString());
        }

        return this;
    }


}