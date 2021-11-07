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

package org.komunumo.ui.view.website;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.ui.LoadMode;

@CssImport("./themes/komunumo/views/website/twitter-feed.css")
@JavaScript(value = "https://platform.twitter.com/widgets.js", loadMode = LoadMode.LAZY)
public class TwitterFeed extends Div {

    public TwitterFeed() {
        addClassName("twitter-feed");

        final var timeline = new Anchor("https://twitter.com/jugch", "Tweets by jugch");
        timeline.addClassName("twitter-timeline");
        timeline.getElement().setAttribute("data-chrome", "nofooter");
        timeline.getElement().setAttribute("data-tweet-limit", "3");

        add(timeline);
    }
}
