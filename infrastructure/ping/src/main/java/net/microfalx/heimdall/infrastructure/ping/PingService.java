package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;

public class PingService {

    @Autowired
    private PingRepository pingRepository;

    @Autowired
    private PingResultRepository pingResultRepository;

    @Autowired
    private InfrastructureService infrastructureService;


}
