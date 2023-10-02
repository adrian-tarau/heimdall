package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("protocol/address")
@DataSet(model = Address.class, canAdd = false)
public class AddressController extends DataSetController<Address, Integer> {

    @Autowired
    private AddressRepository addressRepository;
}
