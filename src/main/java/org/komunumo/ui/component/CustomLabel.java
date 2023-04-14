package org.komunumo.ui.component;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

@CssImport("./themes/komunumo/views/admin/custom-label.css")
public class CustomLabel extends Label {

    @Serial
    private static final long serialVersionUID = 3965979458964907324L;

    public CustomLabel(@NotNull final String text) {
        super(text);
        addClassName("custom-label");
    }
}
