import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.Simulator;

public class GatlingSimulatorProvider implements Simulator.Provider {

    @Override
    public boolean supports(Simulation simulation) {
        return simulation.getType() == Simulation.Type.GATLING;
    }

    @Override
    public Simulator create(Simulation simulation) {
        return new GatlingSimulator(simulation);
    }

    @Override
    public String getName() {
        return "Gatling";
    }
}
