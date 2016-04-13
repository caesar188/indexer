package com.ai.apaking.indexer.actor;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.ai.apaking.indexer.Context;

/**
 * 类型说明: Cluster Listener listening the apaking cluster to find which menber is up,and which is down.
 *
 * @author guh
 *         create time 2016/3/16 9:59
 */
public class ClusterListener extends UntypedActor {

    private static final String NAMING_ROLE="naming_service";

    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    Cluster cluster = Cluster.get(getContext().system());

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.MemberEvent.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ClusterEvent.CurrentClusterState) {

            ClusterEvent.CurrentClusterState state = (ClusterEvent.CurrentClusterState) message;
            log.info(state.getMembers().toString());
            for (Member member : state.getMembers()) {
                if (member.status().equals(MemberStatus.up())&&member.hasRole(NAMING_ROLE)) {
                        Context.getContext().addNamingService(member);
                }
            }

        } else if (message instanceof ClusterEvent.MemberUp) {
            Member member = ((ClusterEvent.MemberUp) message).member();
            if (member.hasRole(NAMING_ROLE)) {
                Context.getContext().addNamingService(member);
            }

        } else if (message instanceof ClusterEvent.MemberRemoved) {
            Member member = ((ClusterEvent.MemberRemoved) message).member();

            if (member.hasRole(NAMING_ROLE)) {
                Context.getContext().removeNamingService(member);
            }

        } else {
            unhandled(message);
        }


    }

}
