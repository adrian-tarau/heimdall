package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.jpa.GelfEvent;
import net.microfalx.heimdall.protocol.jpa.GelfEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/gelf")
@DataSet(model = GelfEvent.class)
public class GelfController extends ProtocolController<GelfEvent> {

    @Autowired
    private GelfEventRepository gelfRepository;
}