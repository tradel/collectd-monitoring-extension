/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.extensions.collectd;

import static com.appdynamics.extensions.yml.YmlReader.readFromFile;
import static com.appdynamics.extensions.collectd.util.ConfigUtils.resolvePath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.appdynamics.extensions.collectd.config.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.jcollectd.agent.protocol.Network;


public class CollectdMonitor extends AManagedMonitor {

    public static final Logger LOGGER = Logger.getLogger(CollectdMonitor.class);
    private ExecutorService executor = null;
    private String metricPrefix = "";
    private ConcurrentMap<String, Long> metricData = new ConcurrentHashMap<String, Long>();
    private boolean sendOnlyChanges = false;

    public CollectdMonitor() {
        LOGGER.info("Collectd Monitor version " + CollectdMonitor.class.getPackage().getImplementationVersion());
    }

    private void configure(Map<String, String> args) {
        if (args != null) {
            String configFilename = resolvePath(args.get("config-file"));
            Configuration conf = readFromFile(configFilename, Configuration.class);

            if (StringUtils.isNotBlank(conf.getMetricPrefix())) {
                LOGGER.info("Setting metric prefix to " + conf.getMetricPrefix());
                metricPrefix = conf.getMetricPrefix();
            }

            if (StringUtils.isNotBlank(conf.getListenAddress())) {
                LOGGER.info("Setting listener address to " + conf.getListenAddress());
                System.setProperty(Network.KEY_PREFIX + "laddr", conf.getListenAddress());
            }

            if (StringUtils.isNotBlank(conf.getInterfaceAddress())) {
                LOGGER.info("Setting interface address to " + conf.getInterfaceAddress());
                System.setProperty(Network.KEY_PREFIX + "ifaddr", conf.getInterfaceAddress());
            }

            this.sendOnlyChanges = conf.isSendOnlyChanges();
        }
    }

    public TaskOutput execute(Map<String, String> args, TaskExecutionContext taskExecutionContext)
            throws TaskExecutionException {

        LOGGER.info("Starting Collectd Monitoring Task");

        configure(args);

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
            executor.submit(new MachineAgentUdpReceiver(metricData));
        }

        LOGGER.info(String.format("Publishing %d metrics", metricData.size()));

        for (Map.Entry<String, Long> metric : metricData.entrySet()) {
            String key = metricPrefix + metric.getKey().replace('/', '|');
            getMetricWriter(key).printMetric(Long.toString(metric.getValue()));
        }

        if (sendOnlyChanges) {
            LOGGER.info("Clearing metric cache");
            metricData.clear();
        }

        return new TaskOutput("Success");
    }

    @Override
    public void stop() {
        if (executor != null) {
            try {
                LOGGER.info("Shutting down listener");
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                LOGGER.warn("Listener did not shut down within the timeout period");
            }
            finally {
                executor.shutdownNow();
                LOGGER.info("Listener terminated");
            }
        }
        super.stop();
    }


    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        CollectdMonitor monitor = new CollectdMonitor();
        monitor.execute(params, null);
    }}
