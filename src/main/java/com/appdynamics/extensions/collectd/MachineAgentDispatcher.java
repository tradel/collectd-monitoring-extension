/**
 * @author Todd Radel <tradel@appdynamics.com>
 */

package com.appdynamics.extensions.collectd;

import org.apache.log4j.Logger;
import org.jcollectd.agent.api.DataSource;
import org.jcollectd.agent.api.Notification;
import org.jcollectd.agent.api.Values;
import org.jcollectd.agent.protocol.Dispatcher;
import org.jcollectd.agent.protocol.TypesDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MachineAgentDispatcher implements Dispatcher {

    public static final Logger LOGGER = Logger.getLogger(MachineAgentDispatcher.class);
    private Map<String, PreviousValue> previousValues = new HashMap<String, PreviousValue>();
    private ConcurrentMap<String, Long> metrics;

    protected class PreviousValue {
        long time;
        long value;
        PreviousValue(long t, long v) { time = t; value = v; }
    }

    public MachineAgentDispatcher(ConcurrentMap<String, Long> metrics) {
        LOGGER.info("Created dispatcher");
        this.metrics = metrics;
    }

    public void dispatch(Values values) {
        assert values != null;
        assert values.getData() != null;
        assert values.getDataSource() != null;

        int size = values.getData().size();
        List<DataSource> sources = values.getDataSource();
//        String src = values.getSource();

        if (sources == null || sources.size() == 0) {
            LOGGER.warn("Data source is null or empty, value type = " + values.getType());
            sources = TypesDB.getInstance().getType(values.getType());
            if (sources == null) {
                LOGGER.warn("TypesDB returned null for " + values.getType());
                return;
            } else if (sources.size() == 0) {
                LOGGER.warn("TypesDB returned no records for " + values.getType());
                return;
            }
        }

        assert sources.size() == size;

        for (int i=0; i<size; i++) {
            DataSource ds = sources.get(i);
            String src = values.getSource() + "/" + values.getDataSource().get(i).getName();

            if (ds.getType() == DataSource.Type.DERIVE.value()) {
                LOGGER.debug(src + " is a derived type");
                long timeNew = values.getTime();
                long valueNew = values.getData().get(i).longValue();
                if (previousValues.containsKey(src)) {
                    PreviousValue previous = previousValues.get(src);
                    long value = valueNew - previous.value;
                    long interval = timeNew - previous.time;
                    if (values.isHires()) {
                        LOGGER.debug(src + " is a hires timer");
                        interval >>= 30;
                    }
                    LOGGER.debug(String.format("valueOld=%d valueNew=%d diff=%d timeOld=%d timeNew=%d interval=%s",
                            previous.value, valueNew, value, previous.time, timeNew, Long.toString(interval)));
                    value /= interval;
                    metrics.put(src, value);
                } else {
                    LOGGER.debug(src + " has no previous data point, waiting for another");
                }
                previousValues.put(src, new PreviousValue(timeNew, valueNew));
            } else if (ds.getType() == DataSource.Type.GAUGE.value()) {
                // GAUGE values are reported as double, so convert to long
                metrics.put(src, (long) values.getData().get(i).doubleValue());
            } else if (ds.getType() == DataSource.Type.COUNTER.value()) {
                // TODO: handle COUNTER data type
            } else if (ds.getType() == DataSource.Type.ABSOLUTE.value()) {
                // TODO: handle ABSOLUTE data type
            } else {
                LOGGER.error(String.format("Unknown data source type: ", ds.getType()));
            }
        }
    }

    public void dispatch(Notification notification) {
        LOGGER.info("Received notification");
    }
}
