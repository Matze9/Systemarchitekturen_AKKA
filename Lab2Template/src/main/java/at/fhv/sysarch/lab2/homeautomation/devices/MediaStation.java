package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.blackboard.Blackboard;

public class MediaStation extends AbstractBehavior<MediaStation.MediaCommand> {


    ////////////COMMANDS/////////////////////
    public interface MediaCommand{}

    public static class playMovie implements MediaCommand {
        final boolean isplaying = true;
    }

    public static class stopMovie implements MediaCommand {
        final boolean isPlaying = false;
    }



    ////////////MEDIASTATION/////////////////////

    private ActorRef<Blackboard.BlackBoardCommand> blackboard;
    private boolean isPlaying = false;

    public static Behavior<MediaStation.MediaCommand> create(ActorRef<Blackboard.BlackBoardCommand> blackboard){
        return Behaviors.setup(context -> new MediaStation(context, blackboard));
    }

    private MediaStation(ActorContext<MediaCommand> context, ActorRef<Blackboard.BlackBoardCommand> blackboard) {
        super(context);
        this.blackboard = blackboard;
    }

    @Override
    public Receive<MediaCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MediaStation.playMovie.class, this::onPlayMovie)
                .onMessage(MediaStation.stopMovie.class, this::onStopMovie)
                .build();
    }

    private Behavior<MediaStation.MediaCommand> onPlayMovie(MediaStation.playMovie command) {
        getContext().getLog().info("Mediastation started movie.");
        this.blackboard.tell(new Blackboard.startStopMovie(command.isplaying));

        return this;
    }

    private Behavior<MediaStation.MediaCommand> onStopMovie(MediaStation.stopMovie command) {
        getContext().getLog().info("Mediastation stopped movie.");
        this.blackboard.tell(new Blackboard.startStopMovie(command.isPlaying));

        return this;
    }



}
