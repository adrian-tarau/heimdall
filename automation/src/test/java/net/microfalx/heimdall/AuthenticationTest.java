package net.microfalx.heimdall;

import net.microfalx.bootstrap.serenity.Logout;
import net.microfalx.bootstrap.serenity.task.Login;
import org.junit.jupiter.api.Test;

public class AuthenticationTest extends AbstractSystemTestCase {

    @Test
    void loginAsAdmin() {
        toby.attemptsTo(Login.as(getAdminUserName(), getAdminPassword()));
    }

    @Test
    void logout() {
        loginAsAdmin();
        toby.attemptsTo(Logout.logout());
    }
}
