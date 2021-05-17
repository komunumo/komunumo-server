package org.komunumo.views.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class WrapperCard extends Div {

    public WrapperCard(final String className, final Component[] components, final String... classes) {
        addClassName(className);

        final var card = new Div();
        card.addClassNames(classes);
        card.add(components);

        add(card);
    }

}
