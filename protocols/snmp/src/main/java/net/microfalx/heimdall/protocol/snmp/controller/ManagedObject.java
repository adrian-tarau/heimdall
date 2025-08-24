package net.microfalx.heimdall.protocol.snmp.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.annotation.*;

@Name("Managed Objects")
@Getter
@Setter
@ReadOnly
public class ManagedObject extends NamedIdentityAware<String> {

    @Position(10)
    @Description("The type of the managed object")
    @Width("100px")
    @NotBlank
    private String type;

    @Position(11)
    @Label(value = "Rows")
    @Description("The number of columns if the object is a table")
    @Width("80px")
    @NotBlank
    private int rowCount;

    @Position(12)
    @Label(value = "Columns")
    @Description("The number of columns if the object is a table")
    @Width("80px")
    @NotBlank
    private int columnCount;

    @Position(20)
    @Description("The value of the managed object")
    @Width("150px")
    @Filterable()
    private String value;
}
