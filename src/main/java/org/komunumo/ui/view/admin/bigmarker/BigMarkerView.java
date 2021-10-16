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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.MemberService;
import org.komunumo.ui.component.ResizableView;
import org.komunumo.ui.view.admin.AdminLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        final var grid = new Grid<String[]>();

        final var firstNameColumnField = new Select<ColumnIdentifier>();
        firstNameColumnField.setLabel("First name column");
        final var lastNameColumnField = new Select<ColumnIdentifier>();
        lastNameColumnField.setLabel("Last name column");
        final var emailColumnField = new Select<ColumnIdentifier>();
        emailColumnField.setLabel("Email column");
        final var registrationDateColumnField = new Select<ColumnIdentifier>();
        registrationDateColumnField.setLabel("Registration date column");
        final var startImport = new Button("Import");
        final var columnConfig = new Div(
                firstNameColumnField,
                lastNameColumnField,
                emailColumnField,
                registrationDateColumnField,
                startImport
        );
        columnConfig.setEnabled(false);


        final var buffer = new MemoryBuffer();
        final var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(event -> {
            final var filename = event.getFileName();
            final var inputStream = buffer.getInputStream();

            try {
                final var workbook = new XSSFWorkbook(inputStream);
                final var sheet = workbook.getSheet("attend list");

                final var bigmarkerUrl = sheet.getRow(7).getCell(1).getStringCellValue();
                final var totalAttendees = Integer.parseUnsignedInt(
                        sheet.getRow(13).getCell(1).getStringCellValue());

                final var firstAttendeeRow = findFirstAttendeeRow(sheet);
                if (firstAttendeeRow == -1) {
                    Notification.show("Could not identify first attendee row!");
                    return;
                }
                final var cellCount = getCellCount(sheet, firstAttendeeRow, totalAttendees);
                final var items = readItems(sheet, firstAttendeeRow, totalAttendees, cellCount);

                final var headerRow = sheet.getRow(firstAttendeeRow - 1);
                final var headerText = new String[cellCount + 1];
                for (int i = 0; i < cellCount; i++) {
                    headerText[i] = headerRow.getCell(i).getStringCellValue();
                }
                for (int i = 0; i < cellCount; i++) {
                    final var index = i;
                    grid.addColumn(data -> data[index])
                            .setHeader(headerText[index])
                            .setAutoWidth(true);
                }
                grid.setItems(items);
                Notification.show(String.format(
                        "Excel file '%s' successfully parsed.", filename));

                final var columnIdentifiers = new ArrayList<ColumnIdentifier>();
                columnIdentifiers.add(new ColumnIdentifier(-1, ""));
                for (var i = 0; i < headerText.length; i++) {
                    final var text = headerText[i];
                    if (text != null && !text.isBlank()) {
                        columnIdentifiers.add(new ColumnIdentifier(i, text));
                    }
                }
                firstNameColumnField.setItems(columnIdentifiers);
                lastNameColumnField.setItems(columnIdentifiers);
                emailColumnField.setItems(columnIdentifiers);
                registrationDateColumnField.setItems(columnIdentifiers);
                guessColumns(columnIdentifiers,
                        firstNameColumnField, lastNameColumnField, emailColumnField, registrationDateColumnField);
                startImport.addClickListener(buttonClickEvent -> importAttendees(bigmarkerUrl, items,
                        firstNameColumnField, lastNameColumnField, emailColumnField, registrationDateColumnField));
                columnConfig.setEnabled(true);
            } catch (final IOException e) {
                Notification.show(e.getMessage());
            }
        });
        upload.addFileRejectedListener(event -> Notification.show(event.getErrorMessage()));

        return new Div(
                title, upload, grid, columnConfig
        );
    }

    private void guessColumns(
            @NotNull final ArrayList<ColumnIdentifier> columnIdentifiers,
            @NotNull final Select<ColumnIdentifier> firstNameColumnField,
            @NotNull final Select<ColumnIdentifier> lastNameColumnField,
            @NotNull final Select<ColumnIdentifier> emailColumnField,
            @NotNull final Select<ColumnIdentifier> registrationDateColumnField) {
        firstNameColumnField.setValue(findColumn(columnIdentifiers, "First Name"));
        lastNameColumnField.setValue(findColumn(columnIdentifiers, "Last Name"));
        emailColumnField.setValue(findColumn(columnIdentifiers, "Email"));
        registrationDateColumnField.setValue(findColumn(columnIdentifiers, "Registration Date"));
    }

    private ColumnIdentifier findColumn(@NotNull final ArrayList<ColumnIdentifier> columnIdentifiers, @NotNull final String columnTitle) {
        for (final ColumnIdentifier columnIdentifier : columnIdentifiers) {
            if (columnIdentifier.getTitle().contains(columnTitle)) {
                return columnIdentifier;
            }
        }
        return columnIdentifiers.get(0);
    }

    private void importAttendees(
            @NotNull final String bigmarkerUrl,
            @NotNull final List<String[]> items,
            @NotNull final Select<ColumnIdentifier> firstNameColumnField,
            @NotNull final Select<ColumnIdentifier> lastNameColumnField,
            @NotNull final Select<ColumnIdentifier> emailColumnField,
            @NotNull final Select<ColumnIdentifier> registrationDateColumnField) {
        if (firstNameColumnField.getValue().getIndex() == -1) {
            Notification.show("Configure the first name column!");
        }
        if (lastNameColumnField.getValue().getIndex() == -1) {
            Notification.show("Configure the last name column!");
        }
        if (emailColumnField.getValue().getIndex() == -1) {
            Notification.show("Configure the email column!");
        }
        if (registrationDateColumnField.getValue().getIndex() == -1) {
            Notification.show("Configure the registration date column!");
        }
    }

    private List<String[]> readItems(@NotNull final XSSFSheet sheet, final int firstAttendeeRow, final int totalAttendees, final int cellCount) {
        final var items = new ArrayList<String[]>();
        for (int rowNum = firstAttendeeRow; rowNum < firstAttendeeRow + totalAttendees; rowNum++) {
            final var rowData = new String[cellCount];
            final var row = sheet.getRow(rowNum);
            for (int cellNum = 0; cellNum <= cellCount; cellNum++) {
                String cellValue = "";
                final var cell = row.getCell(cellNum);
                if (cell == null) {
                    continue;
                }
                switch (cell.getCellType()) {
                    case NUMERIC: cellValue = Double.toString(cell.getNumericCellValue()); break;
                    case STRING: cellValue = cell.getStringCellValue(); break;
                    case BOOLEAN: cellValue = Boolean.toString(cell.getBooleanCellValue()); break;
                }
                rowData[cellNum] = cellValue;
            }
            items.add(rowData);
        }
        return items;
    }

    private int getCellCount(@NotNull final XSSFSheet sheet, final int firstAttendeeRow, final int totalAttendees) {
        var cellCount = 0;
        for (int rowNum = firstAttendeeRow; rowNum < firstAttendeeRow + totalAttendees; rowNum++) {
            final var row = sheet.getRow(rowNum);
            for (final Cell cell : row) {
                final var columnIndex = cell.getColumnIndex();
                if (columnIndex + 1 > cellCount) {
                    cellCount = columnIndex + 1;
                }
            }
        }
        return cellCount;
    }

    private int findFirstAttendeeRow(@NotNull final XSSFSheet sheet) {
        for (final Row row : sheet) {
            final var cell = row.getCell(0);
            final var cellType = cell.getCellType();
            if (CellType.NUMERIC.equals(cellType)) {
                final var value = cell.getNumericCellValue();
                if (Math.round(value) == 1) {
                    return row.getRowNum();
                }
            } else if (CellType.STRING.equals(cellType)) {
                final var value = cell.getStringCellValue();
                if ("1".equals(value)) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    private static class ColumnIdentifier {
        private final int index;
        private final String title;

        public ColumnIdentifier(final int index, @NotNull final String title) {
            this.index = index;
            this.title = title;
        }

        public int getIndex() {
            return index;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
