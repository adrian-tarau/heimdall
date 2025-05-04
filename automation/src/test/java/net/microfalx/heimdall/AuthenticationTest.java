package net.microfalx.heimdall;

import net.microfalx.bootstrap.serenity.junit.AbstractSystemTestCase;
import net.microfalx.bootstrap.serenity.task.Logout;
import net.microfalx.bootstrap.serenity.task.User;
import org.junit.jupiter.api.Test;

public class AuthenticationTest extends AbstractSystemTestCase {

    @Test
    void loginAsAdmin() {
        actor.attemptsTo(User.asAdmin().login());
    }

    @Test
    void loginAsAdminAndLogoutWithLink() {
        loginAsAdmin();
        actor.attemptsTo(Logout.withLink());
    }

    @Test
    void loginAsAdminAndLogoutWithButton() {
        loginAsAdmin();
        actor.attemptsTo(Logout.withButton());
    }

    @Test
    void loginAsRegular() {
        actor.attemptsTo(User.asRegular());
    }

    @Test
    void loginAsGuest() {
        actor.attemptsTo(User.asGuest());
    }
}
