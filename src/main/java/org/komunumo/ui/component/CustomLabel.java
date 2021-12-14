package org.komunumo.ui.component;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import org.jetbrains.annotations.NotNull;

@CssImport("./themes/komunumo/views/admin/custom-label.css")
public class CustomLabel extends Label {

    public CustomLabel(@NotNull final String text) {
        super(text);
        addClassName("custom-label");
    }
}
