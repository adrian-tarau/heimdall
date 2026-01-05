package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

@Controller
@RequestMapping("/broker/event")
@DataSet(model = BrokerEvent.class, viewTemplate = "broker/view_event", viewClasses = "modal-xl")
@Help("/broker/event")
public class BrokerEventController extends DataSetController<BrokerEvent, Integer> {

    private final ContentService contentService;

    public BrokerEventController(DataSetService dataSetService, ContentService contentService) {
        super(dataSetService);
        this.contentService = contentService;
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<BrokerEvent, Field<BrokerEvent>, Integer> dataSet, Model controllerModel, BrokerEvent dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            dataSetModel.setEventName(net.microfalx.lang.StringUtils.removeLineBreaks(dataSetModel.getEventName()));
        }
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<BrokerEvent, Field<BrokerEvent>, Integer> dataSet, Model controllerModel, BrokerEvent dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);

        Content content = getContent(dataSetModel);
        Attributes<?> externalAttributes = content.getExternalAttributes();
        controllerModel.addAttribute("content", content);
        controllerModel.addAttribute("mimeType", content.getMimeType());
        controllerModel.addAttribute("badges", externalAttributes);
        controllerModel.addAttribute("fields", content.getAttributes());
    }

    private Content getContent(BrokerEvent dataSetModel) {
        URI eventUri = BrokerUtils.getEventUri(dataSetModel.getSession().getResource(), dataSetModel.getEventId());
        return contentService.resolve(ContentLocator.create(dataSetModel.getEventId(), "event", eventUri));
    }

}
