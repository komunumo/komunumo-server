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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.upload.Upload;

import elemental.json.Json;

import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.util.UriUtils;

public class ImageUploadField extends Div {

    private final Image preview;

    public ImageUploadField(@NotNull final String label) {

        preview = new Image();
        preview.setWidth("100%");
        final var upload = new Upload();
        upload.getStyle().set("box-sizing", "border-box");
        upload.getElement().appendChild(preview.getElement());

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

        add(new Label(label), new Div(upload));
    }

    public String getValue() {
        return preview.getSrc();
    }

    public void setValue(@NotNull final String value) {
        preview.setSrc(value);
    }
}
