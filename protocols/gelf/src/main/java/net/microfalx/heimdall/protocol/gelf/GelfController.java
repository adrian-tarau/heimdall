package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.bootstrap.model.AttributeUtils.shouldDisplayAsBadge;

@Controller
@RequestMapping("/protocol/gelf")
@DataSet(model = GelfEvent.class, viewTemplate = "gelf/event_view", viewClasses = "modal-xl", tags = {"ai", "gelf"})
@Help("protocol/gelf")
public class GelfController extends ProtocolController<GelfEvent> {

    @Autowired
    private GelfEventRepository gelfRepository;

    @Autowired
    private GelfService gelfService;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<GelfEvent, Field<GelfEvent>, Integer> dataSet,
                              Model controllerModel, GelfEvent dataSetModel) {
        Attributes<?> attributes = gelfService.getAttributes(dataSetModel);
        Attributes badgeAttributes = Attributes.create();
        Attributes fieldAttributes = Attributes.create();
        for (Attribute attribute : attributes) {
            if (shouldDisplayAsBadge(attribute, true)) {
                badgeAttributes.add(attribute);
            }
            fieldAttributes.add(attribute);
        }
        controllerModel.addAttribute("complex", !fieldAttributes.isEmpty() || dataSetModel.getLongMessage() != null);
        controllerModel.addAttribute("badges", badgeAttributes);
        controllerModel.addAttribute("fields", fieldAttributes);
    }
}
