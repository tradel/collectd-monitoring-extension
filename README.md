CollectdMonitor
===============

## Introduction ##

An AppDynamics Machine Agent extension to bring metrics from [collectd][] 
into AppDynamics. 

collectd is a daemon which collects system performance statistics periodically 
and provides mechanisms to store the values in a variety of ways, for example 
in RRD files.

This extension requires the Java Machine Agent.


## How It Works ##

When it starts, the extension begins listening on a UDP port like a standard
collectd server, as described in the [collectd networking docs][]. Any metrics
that are sent to it will be imported into AppDynamics.

#### Metric Paths

The collectd metric path will be kept intact and created under "Application
Infrastructure Performance" in AppDynamics.  

**Example:**

- collectd: `Metrics/myhost/cpu-0/cpu-user`
- AppDynamics: `Application Infrastructure Performance|MyTier|Custom Metrics|collectd|myhost|cpu-0|cpu-user`

#### Data Types

Collectd currently supports four types of metrics. Here is how the extension handles each type:

- **`GAUGE`** - converted to long integer.
- **`DERIVE`** - derived using the standard collectd formula and stored as long integer.
- **`COUNTER`** - currently not handled by the extension.
- **`ABSOLUTE`** - currently not handled by the extension.


## Installation ##

1. Download CollectdMonitor.zip from the Community.
2. Copy CollectdMonitor.zip into the directory where you installed the machine 
   agent, under `$AGENT_HOME/monitors`.
3. Unzip the file. This will create a new directory called `CollectdMonitor`.
4. In `$AGENT_HOME/monitors/CollectdMonitor`, edit the file `config.yaml` to 
   configure the extension.
5. Restart the machine agent.


## Configuration ##

The extension is configured via a [YAML][] file in the `conf` subdirectory of the
extension. Here is the default configuration:

```yaml
# prefix used to create metrics in AppDynamics
metricPrefix:  "Custom Metrics|collectd|"

# IP address and port number for the UDP listener
listenAddress: "0.0.0.0:25628"

# IP address bound to the multicast interface
# interfaceAddress: "127.0.0.1"

sendOnlyChanges: false
```




Once you have set up the extension, you can then configure collectd to start
sending metrics to it. Load the network plugin and add a stanza in 
`collectd.conf` to the IP address and port you configured above. For example,
assuming your machine agent is running on 192.168.1.15:

```
LoadPlugin network

<Plugin network>
    <Server "192.168.1.15" "25628">
        Interface "eth0"
    </Server>
</Plugin>

```


## Caution

This monitor can potentially register hundreds or thousands of new metrics, 
depending on how many metrics you import from collectd. By default, the Machine 
Agent will only report 200 metrics to the controller, so you may need to 
increase that limit when installing this monitor. To increase the metric 
limit, you must add a parameter when starting the Machine Agent, like this:

    java -Dappdynamics.agent.maxMetrics=1000 -jar machineagent.jar


## Support

For any questions or feature requests, please open a [support ticket][].


**Version:** 1.0-SNAPSHOT  
**Controller Compatibility:** 3.6 or later    
**Last Updated:** 01-Mar-2016  
**Author:** Todd Radel  

------------------------------------------------------------------------------

## Release Notes

Please see the file `CHANGES.md`.


[collectd]: https://collectd.org/
[collectd networking docs]: https://collectd.org/wiki/index.php/Networking_introduction
[YAML]: http://yaml.org/
[support ticket]: https://help.appdynamics.com/