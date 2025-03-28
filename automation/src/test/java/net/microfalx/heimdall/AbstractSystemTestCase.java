package net.microfalx.heimdall;

import net.microfalx.bootstrap.serenity.extension.BoostrapExtension;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.annotations.CastMember;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BoostrapExtension.class, SerenityJUnit5Extension.class,})
public abstract class AbstractSystemTestCase {

    @CastMember(name = "Toby")
    protected Actor toby;

    protected final String getAdminUserName() {
        return "admin";
    }

    protected final String getAdminPassword() {
        return System.getProperty("test.admin.password", "dummy");
    }
}
