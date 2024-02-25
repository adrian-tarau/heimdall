package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Optional;

/**
 * Base class for all protocol controllers.
 *
 * @param <E> the event type
 */
public abstract class ProtocolController<E extends Event> extends DataSetController<E, Integer> {

    @Autowired
    private PartRepository partRepository;

    @GetMapping(value = "part/{partId}/view")
    @ResponseBody()
    public final ResponseEntity<InputStreamResource> viewPart(Model model, @PathVariable("partId") String partId) throws IOException {
        Resource resource = findResource(Integer.parseInt(partId));
        if (!resource.exists())
            resource = MemoryResource.create("A part with identifier '" + partId + "' does not exist");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(resource.getMimeType()))
                .body(new InputStreamResource(resource.getInputStream()));
    }

    @GetMapping(value = "part/{partId}/download")
    @ResponseBody()
    public final ResponseEntity<InputStreamResource> downloadPart(Model model, @PathVariable("partId") String partId) throws IOException {
        Resource resource = findResource(Integer.parseInt(partId));
        if (!resource.exists()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(resource.getMimeType()))
                .header("Content-Disposition", "attachment; filename=\"" + resource.getName() + "\"")
                .body(new InputStreamResource(resource.getInputStream(true)));
    }

    private Resource findResource(int partId) {
        Optional<net.microfalx.heimdall.protocol.core.jpa.Part> partOptional = partRepository.findById(partId);
        if (partOptional.isEmpty()) return NullResource.NULL;
        net.microfalx.heimdall.protocol.core.jpa.Part part = partOptional.get();
        Resource resource = ResourceFactory.resolve(part.getResource()).withMimeType(part.getMimeType());
        if (StringUtils.isNotEmpty(part.getFileName())) resource = resource.withName(part.getFileName());
        return resource;
    }
}
