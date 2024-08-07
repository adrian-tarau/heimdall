package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/protocol/address")
@DataSet(model = Address.class, timeFilter = false, canAdd = false, canDelete = false)
@Help("protocol/address")
public class AddressController extends SystemDataSetController<Address, Integer> {

    @Autowired
    private AddressRepository addressRepository;
}
