package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;

import java.io.IOException;

import static net.microfalx.heimdall.rest.api.Library.getNaturalId;
import static net.microfalx.heimdall.rest.api.RestConstants.SCRIPT_ATTR;

public abstract class AbstractLibraryController<T extends AbstractLibrary> extends DataSetController<T, Integer> {

    protected abstract RestService getRestService();

    protected abstract ContentService getContentService();

    protected final Simulation discover(Resource resource) {
        try {
            return getRestService().discover(resource);
        } catch (Exception e) {
            throw new DataSetException("Invalid library type '" + resource.getName() + "'");
        }
    }

    protected final Resource register(Resource resource) throws IOException {
        return getRestService().registerResource(resource.withAttribute(SCRIPT_ATTR, Boolean.TRUE));
    }

    @Override
    protected boolean beforePersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        Resource resolve = ResourceFactory.resolve(model.getResource());
        if (state == State.ADD) {
            model.setNaturalId(getNaturalId(model.getType(), resolve, model.getProject() != null ? model.getProject().getNaturalId() : null));
        }
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        super.afterPersist(dataSet, model, state);
        getRestService().reload();
    }
}
