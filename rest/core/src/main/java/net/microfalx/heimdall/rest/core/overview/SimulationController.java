package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationController")
@DataSet(model = Simulation.class, range = {"last week", "today"})
@RequestMapping("/rest/simulation")
public class SimulationController extends DataSetController<Simulation,Integer> {

    public SimulationController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
