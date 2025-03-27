package net.microfalx.heimdall;

import net.microfalx.bootstrap.serenity.Login;
import org.junit.jupiter.api.Test;

public class LoginTest extends AbstractSystemTestCase {

    @Test
    void loginAsAdmin() {
        toby.attemptsTo(Login.as(getAdminUserName(), getAdminPassword()));
    }
}
