package net.microfalx.heimdall.broker.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.archive.ArchiveResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/broker/session")
@DataSet(model = BrokerSession.class, viewTemplate = "broker/view_session", viewClasses = "modal-xl", defaultQuery = "status != 'Canceled'")
@Help("/broker/session")
@Slf4j
public class BrokerSessionController extends DataSetController<BrokerSession, Integer> {

    public BrokerSessionController(DataSetService dataSetService) {
        super(dataSetService);
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<BrokerSession, Field<BrokerSession>, Integer> dataSet, Model controllerModel, BrokerSession dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);

        try {
            BrokerTopicSnapshot snapshot = getSnapshot(dataSetModel);
            controllerModel.addAttribute("events", snapshot.getEvents());
            controllerModel.addAttribute("fields", collectAttributes(snapshot));
            controllerModel.addAttribute("snapshot", snapshot);
        } catch (IOException e) {
            String message = "Could not extract events from session " + dataSetModel.getId();
            controllerModel.addAttribute("failure", message);
            LOGGER.error(message, e);
        }
    }

    private BrokerTopicSnapshot getSnapshot(BrokerSession dataSetModel) throws IOException {
        Resource resource = ResourceFactory.resolve(dataSetModel.getResource());
        resource = ArchiveResource.create(resource);
        BrokerTopicSnapshot snapshot = new BrokerTopicSnapshot();
        resource.walk((root, child) -> {
            BrokerTopicSnapshot.Event event = BrokerTopicSnapshot.Event.deserialize(child.getInputStream());
            snapshot.add(event);
            snapshot.add(event.getSize());
            return true;
        });
        return snapshot;
    }

    private Collection<AttributeValues> collectAttributes(BrokerTopicSnapshot snapshot) {
        Map<String, AttributeValues> values = new HashMap<>();
        for (BrokerTopicSnapshot.Event event : snapshot.getEvents()) {
            for (Map.Entry<String, Object> entry : event.getAttributes().entrySet()) {
                AttributeValues attributeValues = values.computeIfAbsent(entry.getKey(), AttributeValues::new);
                attributeValues.values.add(ObjectUtils.toString(entry.getValue()));
            }
        }
        List<AttributeValues> attributeValues = new ArrayList<>(values.values());
        attributeValues.sort(Comparator.comparing(AttributeValues::getName));
        return attributeValues;
    }

    @Getter
    private static class AttributeValues {

        private final String name;
        private final Set<String> values = new TreeSet<>();

        public AttributeValues(String name) {
            this.name = name;
        }
    }
}
