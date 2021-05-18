package org.komunumo.views.events;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.EventService;
import org.komunumo.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@Route(value = "events/:eventID?/:action?(edit)", layout = MainView.class)
@PageTitle("Events")
public class EventsView extends Div implements BeforeEnterObserver {

    private final String EVENT_ID = "eventID";
    private final String EVENT_EDIT_ROUTE_TEMPLATE = "events/%d/edit";

    private Grid<Event> grid = new Grid<>(Event.class, false);

    private TextField title;
    private TextField speaker;
    private DateTimePicker date;
    private Checkbox visible;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Event> binder;

    private Event event;

    private EventService eventService;

    public EventsView(@Autowired final EventService eventService) {
        this.eventService = eventService;
        addClassNames("events-view", "flex", "flex-col", "h-full");

        // Create UI
        final var splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("title").setAutoWidth(true);
        grid.addColumn("speaker").setAutoWidth(true);
        grid.addColumn("date").setAutoWidth(true);
        final var visibleRenderer = TemplateRenderer.<Event>of(
                "<iron-icon hidden='[[!item.visible]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.visible]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("visible", Event::isVisible);
        grid.addColumn(visibleRenderer).setHeader("Visible").setAutoWidth(true);

        grid.setItems(query -> eventService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(EVENT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(EventsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Event.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.event == null) {
                    this.event = new Event();
                }
                binder.writeBean(this.event);

                eventService.update(this.event);
                clearForm();
                refreshGrid();
                Notification.show("Event details stored.");
                UI.getCurrent().navigate(EventsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the event details.");
            }
        });

    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        final var eventId = event.getRouteParameters().getInteger(EVENT_ID);
        if (eventId.isPresent()) {
            final var eventFromBackend = eventService.get(eventId.get());
            if (eventFromBackend.isPresent()) {
                populateForm(eventFromBackend.get());
            } else {
                Notification.show(String.format("The requested event was not found, ID = %d", eventId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(EventsView.class);
            }
        }
    }

    private void createEditorLayout(final SplitLayout splitLayout) {
        final var editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        final var editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        final var formLayout = new FormLayout();
        title = new TextField("Title");
        speaker = new TextField("Speaker");
        date = new DateTimePicker("Date");
        date.setStep(Duration.ofSeconds(1));
        visible = new Checkbox("Visible");
        visible.getStyle().set("padding-top", "var(--lumo-space-m)");
        final var fields = new Component[]{title, speaker, date, visible};

        for (final Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(final Div editorLayoutDiv) {
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(final SplitLayout splitLayout) {
        final var wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(final Event value) {
        this.event = value;
        binder.readBean(this.event);

    }
}
