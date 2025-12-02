package net.microfalx.heimdall.protocol.syslog.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.heimdall.protocol.core.AbstractPart;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.syslog.SyslogMessage;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MimeType;

import java.io.IOException;
import java.time.ZonedDateTime;

import static net.microfalx.heimdall.protocol.core.Address.create;

public class SyslogMapper extends AbstractRestApiMapper<SyslogEvent, SyslogDTO> {

    @Override
    protected SyslogDTO doToDto(SyslogEvent syslogEvent) {
        SyslogDTO syslogDTO = new SyslogDTO();
        syslogDTO.setId(syslogEvent.getId());
        syslogDTO.setName(syslogEvent.getMessage().getName());
        syslogDTO.setFacility(syslogEvent.getFacility());
        syslogDTO.setSeverity(syslogEvent.getSeverity());
        syslogDTO.setSource(create(syslogEvent.getAddress().getType(), syslogEvent.getAddress().getValue(), syslogEvent.getAddress().getName()));
        syslogDTO.setMessage(Body.create(syslogEvent.getMessage().getResource()));
        syslogDTO.setReceivedAt(syslogEvent.getReceivedAt());
        syslogDTO.setSentAt(syslogEvent.getSentAt());
        return syslogDTO;
    }

    @Override
    protected SyslogEvent doToEntity(SyslogDTO syslogDTO) {
        SyslogEvent syslogEvent = new SyslogEvent();
        try {
            syslogEvent.setId(syslogDTO.getId());

            Address address = new Address();
            address.setValue(syslogDTO.getSource().getValue());
            address.setId((int) syslogDTO.getId());
            address.setDescription(StringUtils.EMPTY_STRING);
            address.setName(syslogDTO.getSource().getName());
            address.setType(syslogDTO.getSource().getType());
            syslogEvent.setAddress(address);

            syslogEvent.setFacility(syslogDTO.getFacility());
            syslogEvent.setSeverity(syslogDTO.getSeverity());

            Part part = new Part();
            part.setId((int) syslogDTO.getId());
            part.setResource(syslogDTO.getMessage().loadAsString());
            part.setType(syslogDTO.getMessage().getType());
            part.setName(syslogDTO.getMessage().getName());
            part.setFileName(syslogDTO.getMessage().getFileName());
            part.setLength((int) syslogDTO.getMessage().getResource().length());
            part.setMimeType(MimeType.get(syslogDTO.getMessage().getResource().getMimeType()));
            syslogEvent.setMessage(part);
        } catch (Exception e) {
            ExceptionUtils.rethrowException(e);
        }
        return syslogEvent;
    }
}
