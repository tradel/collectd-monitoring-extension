/**
 * @author Todd Radel <tradel@appdynamics.com>
 */

package com.appdynamics.extensions.collectd;

import org.apache.log4j.Logger;
import org.jcollectd.server.protocol.UdpReceiver;

import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentMap;

public class MachineAgentUdpReceiver extends UdpReceiver
    implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(MachineAgentUdpReceiver.class);

    public MachineAgentUdpReceiver(ConcurrentMap<String, Long> metrics) {
        super();
        setDispatcher(new MachineAgentDispatcher(metrics));
    }

    private void setup() throws Exception {
        DatagramSocket socket = getSocket();
        if (socket instanceof MulticastSocket) {
            MulticastSocket mcast = (MulticastSocket) socket;
            LOGGER.info("Multicast interface: " + mcast.getInterface());
        }
        LOGGER.info("Listening on " + getListenAddress());
    }

    public void run() {
        try {
            LOGGER.info("Listener started");
            setup();
            listen();
        } catch (InterruptedException e) {
            LOGGER.info("Listener stopping");
            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("Closing socket");
        super.shutdown();
        LOGGER.info("Listener is stopped");
    }


}
