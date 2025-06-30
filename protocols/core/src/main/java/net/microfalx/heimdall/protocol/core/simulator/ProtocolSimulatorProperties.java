package net.microfalx.heimdall.protocol.core.simulator;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.lang.FormatterUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.protocol.simulator")
@Getter
@Setter
public class ProtocolSimulatorProperties {

    /**
     * Whether the protocol simulator is enabled.
     */
    private boolean enabled;

    /**
     * The interval at which events are simulated.
     */
    private Duration interval = Duration.ofSeconds(10);

    /**
     * The rate at which events are simulated, events per second
     */
    private float rate = 0.5f;

    /**
     * The minimum number of events to simulate in one round of simulation.
     */
    private int minimumEventCount = 1;

    /**
     * The maximum number of events to simulate in one round of simulation.
     */
    private int maximumEventCount = 5;

    /**
     * The minimum number of addresses to simulate in one round of simulation.
     */
    private int minimumAddressCount = 20;

    /**
     * The maximum number of addresses to simulate in one round of simulation.
     */
    private int maximumAddressCount = 50;

    /**
     * The minimum length the simulated content
     */
    private int minimumPartLength = (int) (FormatterUtils.K);

    /**
     * The maximum length the simulated content
     */
    private int maximumPartLength = (int) (5 * FormatterUtils.K);

    /**
     * Whether to use available samples for simulation.
     */
    private boolean useSamples = true;

    /**
     * Whether to use external data sets for simulation.
     *
     * If a given simulator does not have its own data set, it will generate data using a random data generator.
     */
    private boolean useExternalDataSets = true;

}
