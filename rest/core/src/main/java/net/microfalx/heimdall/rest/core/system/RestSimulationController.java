package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller("SystemSimulationController")
@DataSet(model = RestSimulation.class, timeFilter = false, canAdd = false, canUpload = true)
@RequestMapping("/system/rest/simulation")
public class RestSimulationController extends DataSetController<RestSimulation, Integer> {

    @Autowired
    private RestService restService;

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<RestSimulation, Field<RestSimulation>, Integer> dataSet, Model model, Resource resource) {
        Simulation simulation;
        try {
            simulation = restService.discover(resource);
        } catch (Exception e) {
            throw new DataSetException("Invalid simulation type '" + resource.getName() + "'");
        }
        try {
            Resource storedResource = restService.registerResource(resource);
            Simulation.Builder builder = new Simulation.Builder(simulation).resource(storedResource);
            restService.registerSimulation(builder.build());
        } catch (IOException e) {
            ExceptionUtils.throwException(e);
        }
    }
}
