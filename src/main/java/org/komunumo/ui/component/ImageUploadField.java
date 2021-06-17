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

package org.komunumo.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Element;
import elemental.json.Json;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ImageUploadField extends CustomField<String> {

    private final Image preview;
    private final Button remove;
    private final Upload upload;

    public ImageUploadField() {
        preview = new Image();
        preview.setWidth("100%");

        remove = new Button("Remove", clickEvent -> setPresentationValue(""));

        upload = new Upload();
        upload.getStyle().set("box-sizing", "border-box");
        upload.getElement().appendChild(preview.getElement());

        final var uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> uploadBuffer);
        upload.addSucceededListener(succeededEvent -> {
            final var mimeType = succeededEvent.getMIMEType();
            final var base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            final var dataUrl = "data:" + mimeType + ";base64," + UriUtils.encodeQuery(base64ImageData, UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            setPresentationValue(dataUrl);
            uploadBuffer.reset();
        });

        add(upload);
    }

    public ImageUploadField(@NotNull final String label) {
        this();
        setLabel(label);
    }

    @Override
    protected String generateModelValue() {
        return preview.getSrc();
    }

    @Override
    protected void setPresentationValue(@NotNull final String value) {
        preview.setSrc(value);
        if (value.isBlank()) {
            removeChild(preview.getElement());
            removeChild(remove.getElement());
        } else {
            appendChild(preview.getElement());
            appendChild(remove.getElement());
        }
    }

    private void appendChild(@NotNull final Element element) {
        upload.getElement().appendChild(element);
    }

    private void removeChild(@NotNull final Element element) {
        if (element.getParent() != null) {
            upload.getElement().removeChild(element);
        }
    }

}
