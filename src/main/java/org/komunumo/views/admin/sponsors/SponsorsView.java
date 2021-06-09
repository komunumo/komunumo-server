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

package org.komunumo.views.admin.sponsors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.komunumo.data.db.enums.SponsorLevel;
import org.komunumo.data.db.tables.records.SponsorRecord;
import org.komunumo.data.service.SponsorService;
import org.komunumo.views.admin.AdminView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

@Route(value = "admin/sponsors/:sponsorID?/:action?(edit)", layout = AdminView.class)
@PageTitle("Sponsor Administration")
public class SponsorsView extends Div implements BeforeEnterObserver {

    private final String SPONSOR_ID = "sponsorID";
    private final String SPONSOR_EDIT_ROUTE_TEMPLATE = "admin/sponsors/%d/edit";

    private final Grid<SponsorRecord> grid = new Grid<>(SponsorRecord.class, false);

    private TextField name;
    private TextField url;
    private Upload logo;
    private Image logoPreview;
    private DatePicker validFrom;
    private DatePicker validTo;
    private Select<SponsorLevel> level;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SponsorRecord> binder;

    private SponsorRecord sponsor;

    private final SponsorService sponsorService;

    public SponsorsView(@Autowired final SponsorService sponsorService) {
        this.sponsorService = sponsorService;
        addClassNames("sponsors-view", "flex", "flex-col", "h-full");

        // Create UI
        final var splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        final var logoRenderer = TemplateRenderer.<SponsorRecord>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src='[[item.logo]]' /></span>")
                .withProperty("logo", SponsorRecord::getLogo);
        grid.addColumn(logoRenderer).setHeader("Logo").setWidth("96px").setFlexGrow(0);

        grid.addColumn("validFrom").setAutoWidth(true);
        grid.addColumn("validTo").setAutoWidth(true);
        grid.addColumn("level").setAutoWidth(true);
        grid.setItems(query -> sponsorService.list(query.getOffset(), query.getLimit()));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SPONSOR_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SponsorsView.class);
            }
        });

        grid.sort(new GridSortOrderBuilder<SponsorRecord>()
                .thenAsc(grid.getColumnByKey("name"))
                .build());

        // Configure Form
        binder = new BeanValidationBinder<>(SponsorRecord.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        attachImageUpload(logo, logoPreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sponsor == null) {
                    this.sponsor = sponsorService.newRecord();
                }
                binder.writeBean(this.sponsor);
                this.sponsor.setLogo(logoPreview.getSrc());

                sponsorService.store(this.sponsor);
                clearForm();
                refreshGrid();
                Notification.show("Sponsor details stored.");
                UI.getCurrent().navigate(SponsorsView.class);
            } catch (final ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sponsor details.");
            }
        });

    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        final var sponsorId = event.getRouteParameters().getLong(SPONSOR_ID);
        if (sponsorId.isPresent()) {
            final var sponsorFromBackend = sponsorService.get(sponsorId.get());
            if (sponsorFromBackend.isPresent()) {
                populateForm(sponsorFromBackend.get());
            } else {
                Notification.show(String.format("The requested sponsor was not found, ID = %d", sponsorId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SponsorsView.class);
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
        name = new TextField("Name");
        url = new TextField("URL");
        final var logoLabel = new Label("Logo");
        logoPreview = new Image();
        logoPreview.setWidth("100%");
        logo = new Upload();
        logo.getStyle().set("box-sizing", "border-box");
        logo.getElement().appendChild(logoPreview.getElement());
        validFrom = new DatePicker("Valid From");
        validTo = new DatePicker("Valid To");
        level = new Select<>(SponsorLevel.values());
        level.setLabel("Level");
        final var fields = new Component[]{name, url, logoLabel, logo, validFrom, validTo, level};

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

    private void populateForm(final SponsorRecord value) {
        this.sponsor = value;
        binder.readBean(this.sponsor);
        this.logoPreview.setVisible(value != null);
        if (value == null) {
            this.logoPreview.setSrc("");
        } else {
            this.logoPreview.setSrc(value.getLogo());
        }

    }
}
