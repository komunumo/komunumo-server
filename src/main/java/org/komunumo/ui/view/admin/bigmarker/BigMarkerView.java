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

package org.komunumo.ui.view.admin.bigmarker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.MemberService;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

@Route(value = "admin/bigmarker", layout = AdminLayout.class)
@PageTitle("BigMarker")
@CssImport(value = "./themes/komunumo/views/admin/bigmarker-view.css")
public class BigMarkerView extends ResizableView {

    private final MemberService memberService;
    private final EventService eventService;
    private final EventMemberService eventMemberService;

    public BigMarkerView(
            @NotNull final MemberService memberService,
            @NotNull final EventService eventService,
            @NotNull final EventMemberService eventMemberService) {
        this.memberService = memberService;
        this.eventService = eventService;
        this.eventMemberService = eventMemberService;

        addClassName("bigmarker-view");
        add(
                new H2("BigMarker"),
                createImportReportComponents()
        );
    }

    private Component createImportReportComponents() {
        final var title = new H3("Import reports");

        final var status = new Status();

        final var buffer = new MultiFileMemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(event -> {
            final var filename = event.getFileName();
            final var inputStream = buffer.getInputStream(event.getFileName());
            status.setStatus(String.format("filename: %s", filename));
        });
        upload.addFileRejectedListener(event -> {
            status.setStatus(event.getErrorMessage());
        });

        return new Div(
                title, upload, status
        );
    }

    private static class Status extends Div {

        public Status() {
            addClassName("status");
        }

        void setStatus(@NotNull final String message) {
            removeAll();
            add(new Paragraph(new Text(message)));
        }

    }

}
