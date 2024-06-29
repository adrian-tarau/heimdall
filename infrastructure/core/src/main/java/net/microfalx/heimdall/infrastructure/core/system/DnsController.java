package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.bootstrap.core.utils.HostnameUtils.isHostname;

@Controller
@RequestMapping("/system/infrastructure/dns")
@DataSet(model = Dns.class, timeFilter = false)
@Help("infrastructure/dns")
public class DnsController extends DataSetController<Dns, Integer> {

    @Autowired
    private InfrastructureService infrastructureService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Dns, Field<Dns>, Integer> dataSet, Dns model, State state) {
        model.setValid(isHostname(model.getHostname()) && isHostname(model.getDomain()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Dns, Field<Dns>, Integer> dataSet, Dns model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
