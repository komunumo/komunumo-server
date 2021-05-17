package org.komunumo.views.members;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Members")
public class MembersView extends Div implements BeforeEnterObserver {

    private final String MEMBER_ID = "memberID";
    private final String MEMBER_EDIT_ROUTE_TEMPLATE = "members/%d/edit";

    private Grid<Member> grid = new Grid<>(Member.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField address;
    private TextField zipCode;
    private TextField city;
    private TextField state;
    private TextField country;
    private DatePicker memberSince;
    private Checkbox admin;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Member> binder;

    private Member member;

    private MemberService memberService;

    public MembersView(@Autowired final MemberService memberService) {
        this.memberService = memberService;
        addClassNames("members-view", "flex", "flex-col", "h-full");

        // Create UI
        final var splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("address").setAutoWidth(true);
        grid.addColumn("zipCode").setAutoWidth(true);
        grid.addColumn("city").setAutoWidth(true);
        grid.addColumn("state").setAutoWidth(true);
        grid.addColumn("country").setAutoWidth(true);
        grid.addColumn("memberSince").setAutoWidth(true);
        final var adminRenderer = TemplateRenderer.<Member>of(
                "<iron-icon hidden='[[!item.admin]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></iron-icon><iron-icon hidden='[[item.admin]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></iron-icon>")
                .withProperty("admin", Member::isAdmin);
        grid.addColumn(adminRenderer).setHeader("Admin").setAutoWidth(true);

        grid.setItems(query -> memberService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MEMBER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MembersView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Member.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.member == null) {
                    this.member = new Member();
                }
                binder.writeBean(this.member);

                memberService.update(this.member);
                clearForm();
                refreshGrid();
                Notification.show("Member details stored.");
                UI.getCurrent().navigate(MembersView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the member details.");
            }
        });

    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        final var memberId = event.getRouteParameters().getInteger(MEMBER_ID);
        if (memberId.isPresent()) {
            final var memberFromBackend = memberService.get(memberId.get());
            if (memberFromBackend.isPresent()) {
                populateForm(memberFromBackend.get());
            } else {
                Notification.show(String.format("The requested member was not found, ID = %d", memberId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MembersView.class);
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
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        address = new TextField("Address");
        zipCode = new TextField("Zip Code");
        city = new TextField("City");
        state = new TextField("State");
        country = new TextField("Country");
        memberSince = new DatePicker("Member Since");
        admin = new Checkbox("Admin");
        admin.getStyle().set("padding-top", "var(--lumo-space-m)");
        final var fields = new Component[]{firstName, lastName, email, address, zipCode, city, state, country,
                memberSince, admin};

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

    private void populateForm(final Member value) {
        this.member = value;
        binder.readBean(this.member);

    }
}
