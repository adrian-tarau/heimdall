package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.protocol.snmp.AgentSimulator;
import net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRule;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/snmp/agent_simulator_rule")
@DataSet(model = AgentSimulatorRule.class, timeFilter = false, canAdd = false, canUpload = true, canDownload = true)
public class AgentSimulatorRuleController extends SystemDataSetController<AgentSimulatorRule, Integer> {

    @Autowired
    private AgentSimulator agentSimulator;

    @Override
    protected Resource download(net.microfalx.bootstrap.dataset.DataSet<AgentSimulatorRule, Field<AgentSimulatorRule>, Integer> dataSet, Model controllerModel, AgentSimulatorRule dataSetModel) {
        return MemoryResource.create(dataSetModel.getContent(), dataSetModel.getName());
    }

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<AgentSimulatorRule, Field<AgentSimulatorRule>, Integer> dataSet, Model model, Resource resource) {
        agentSimulator.persist(resource);
    }
}
