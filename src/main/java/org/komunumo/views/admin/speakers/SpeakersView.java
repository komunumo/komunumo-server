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

package org.komunumo.views.admin.speakers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import org.komunumo.data.db.tables.records.SpeakerRecord;
import org.komunumo.data.service.SpeakerService;
import org.komunumo.views.admin.AdminView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Route(value = "admin/speakers/:speakerID?/:action?(edit)", layout = AdminView.class)
@PageTitle("Speaker Administration")
public class SpeakersView extends Div implements BeforeEnterObserver {

    private final String SPEAKER_ID = "speakerID";
    private final String SPEAKER_EDIT_ROUTE_TEMPLATE = "admin/speakers/%d/edit";

    private final Grid<SpeakerRecord> grid = new Grid<>(SpeakerRecord.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField company;
    private TextArea bio;
    private Upload photo;
    private Image photoPreview;
    private EmailField email;
    private TextField twitter;
    private TextField linkedin;
    private TextField website;
    private TextField address;
    private TextField zipCode;
    private TextField city;
    private TextField state;
    private TextField country;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SpeakerRecord> binder;

    private SpeakerRecord speaker;

    private final SpeakerService speakerService;

    public SpeakersView(@Autowired final SpeakerService speakerService) {
        this.speakerService = speakerService;
        addClassNames("speakers-view", "flex", "flex-col", "h-full");

        // Create UI
        final var splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("company").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("twitter").setAutoWidth(true);

        grid.setItems(query -> speakerService.list(query.getOffset(), query.getLimit()));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SPEAKER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SpeakersView.class);
            }
        });

        grid.sort(new GridSortOrderBuilder<SpeakerRecord>()
                .thenAsc(grid.getColumnByKey("lastName"))
                .thenAsc(grid.getColumnByKey("firstName"))
                .build());

        // Configure Form
        binder = new BeanValidationBinder<>(SpeakerRecord.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        attachImageUpload(photo, photoPreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.speaker == null) {
                    this.speaker = speakerService.newRecord();
                }
                binder.writeBean(this.speaker);
                this.speaker.setPhoto(photoPreview.getSrc());

                speakerService.store(this.speaker);
                clearForm();
                refreshGrid();
                Notification.show("Speaker details stored.");
                UI.getCurrent().navigate(SpeakersView.class);
            } catch (final ValidationException validationException) {
                Notification.show("An exception happened while trying to store the speaker details.");
            }
        });

    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        final var speakerId = event.getRouteParameters().getLong(SPEAKER_ID);
        if (speakerId.isPresent()) {
            final var speakerFromBackend = speakerService.get(speakerId.get());
            if (speakerFromBackend.isPresent()) {
                populateForm(speakerFromBackend.get());
            } else {
                Notification.show(String.format("The requested speaker was not found, ID = %d", speakerId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SpeakersView.class);
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
        company = new TextField("Company");
        bio = new TextArea("Bio");
        final var photoLabel = new Label("Photo");
        photoPreview = new Image();
        photoPreview.setWidth("100%");
        photo = new Upload();
        photo.getStyle().set("box-sizing", "border-box");
        photo.getElement().appendChild(photoPreview.getElement());
        email = new EmailField("Email");
        twitter = new TextField("Twitter");
        linkedin = new TextField("LinkedIn");
        website = new TextField("Website");
        address = new TextField("Address");
        zipCode = new TextField("Zip Code");
        city = new TextField("City");
        state = new TextField("State");
        country = new TextField("Country");
        final var fields = new Component[]{firstName, lastName, company, bio, photoLabel, photo,
                email, twitter, linkedin, website, address, zipCode, city, state, country};

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

    private void attachImageUpload(final Upload upload, final Image preview) {
        final var uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> uploadBuffer);
        upload.addSucceededListener(e -> {
            final var mimeType = e.getMIMEType();
            final var base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            final var dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(final SpeakerRecord value) {
        this.speaker = value;
        binder.readBean(this.speaker);
        this.photoPreview.setVisible(value != null);
        if (value == null) {
            this.photoPreview.setSrc("");
        } else {
            this.photoPreview.setSrc(value.getPhoto());
        }
    }
}
