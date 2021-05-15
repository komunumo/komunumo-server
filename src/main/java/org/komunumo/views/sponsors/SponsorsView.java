package org.komunumo.views.sponsors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.komunumo.data.entity.Sponsor;
import org.komunumo.data.service.SponsorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;

//@Route(value = "sponsors/:sponsorID?/:action?(edit)", layout = MainView.class)
@PageTitle("Sponsors")
public class SponsorsView extends Div implements BeforeEnterObserver {

    private final String SPONSOR_ID = "sponsorID";
    private final String SPONSOR_EDIT_ROUTE_TEMPLATE = "sponsors/%d/edit";

    private Grid<Sponsor> grid = new Grid<>(Sponsor.class, false);

    private TextField name;
    private Upload logo;
    private Image logoPreview;
    private DatePicker validFrom;
    private DatePicker validTo;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Sponsor> binder;

    private Sponsor sponsor;

    private SponsorService sponsorService;

    public SponsorsView(@Autowired SponsorService sponsorService) {
        this.sponsorService = sponsorService;
        addClassNames("sponsors-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        TemplateRenderer<Sponsor> logoRenderer = TemplateRenderer.<Sponsor>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src='[[item.logo]]' /></span>")
                .withProperty("logo", Sponsor::getLogo);
        grid.addColumn(logoRenderer).setHeader("Logo").setWidth("96px").setFlexGrow(0);

        grid.addColumn("validFrom").setAutoWidth(true);
        grid.addColumn("validTo").setAutoWidth(true);
        grid.setItems(query -> sponsorService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
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

        // Configure Form
        binder = new BeanValidationBinder<>(Sponsor.class);

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
                    this.sponsor = new Sponsor();
                }
                binder.writeBean(this.sponsor);
                this.sponsor.setLogo(logoPreview.getSrc());

                sponsorService.update(this.sponsor);
                clearForm();
                refreshGrid();
                Notification.show("Sponsor details stored.");
                UI.getCurrent().navigate(SponsorsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sponsor details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> sponsorId = event.getRouteParameters().getInteger(SPONSOR_ID);
        if (sponsorId.isPresent()) {
            Optional<Sponsor> sponsorFromBackend = sponsorService.get(sponsorId.get());
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

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        Label logoLabel = new Label("Logo");
        logoPreview = new Image();
        logoPreview.setWidth("100%");
        logo = new Upload();
        logo.getStyle().set("box-sizing", "border-box");
        logo.getElement().appendChild(logoPreview.getElement());
        validFrom = new DatePicker("Valid From");
        validTo = new DatePicker("Valid To");
        Component[] fields = new Component[]{name, logoLabel, logo, validFrom, validTo};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
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

    private void populateForm(Sponsor value) {
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
