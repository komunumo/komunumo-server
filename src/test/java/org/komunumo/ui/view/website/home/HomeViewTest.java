/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.komunumo.ui.view.website.home;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.EmailField;
import org.junit.jupiter.api.Test;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.KaribuTest;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomeViewTest extends KaribuTest {

    @Autowired
    private DatabaseService databaseService;

    @Test
    void subscribeToNewsletterWithSuccess() {
        final var correctEmailAddress = "test@komunumo.org";

        UI.getCurrent().navigate(HomeView.class);
        _assertOne(HomeView.class);

        _setValue(_get(EmailField.class), correctEmailAddress);
        _click(_get(Button.class, spec -> spec.withCaption("Subscribe")));

        assertTrue(databaseService.getSubscription(correctEmailAddress).isPresent());

        await().atMost(2, SECONDS).untilAsserted(() -> {
            final var receivedMessage = greenMail.getReceivedMessages()[0];
            assertEquals("Validate your newsletter subscription", receivedMessage.getSubject());
            assertTrue(GreenMailUtil.getBody(receivedMessage).startsWith("Please click on the following link to validate your newsletter subscription:"));
            assertEquals(1, receivedMessage.getAllRecipients().length);
            assertEquals(correctEmailAddress, receivedMessage.getAllRecipients()[0].toString());
        });
    }

    @Test
    void subscribeToNewsletterWithIncompleteEmail() {
        final var incompleteEmailAddress = "test@komunumo";

        UI.getCurrent().navigate(HomeView.class);
        _assertOne(HomeView.class);

        _setValue(_get(EmailField.class), incompleteEmailAddress);
        final var button = _get(Button.class, spec -> spec.withCaption("Subscribe"));
        assertFalse(button.isEnabled());
    }

}
