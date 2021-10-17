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

package org.komunumo.data.importer.bigmarker;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.EventMemberService;
import org.komunumo.data.service.EventService;
import org.komunumo.data.service.MemberService;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

public class BigMarkerReport {

    private final Workbook workbook;

    private final String webinarName;
    private final String webinarUrl;

    private List<BigMarkerRegistration> registrations = null;

    public BigMarkerReport(@NotNull final InputStream inputStream) throws IOException {
        this.workbook = new XSSFWorkbook(inputStream);

        final var summary = workbook.getSheet("summary");
        webinarName = findCell(summary, "Webinar Name").orElseThrow().getStringCellValue();
        webinarUrl = findCell(summary, "URL").orElseThrow().getStringCellValue();
    }

    private Optional<Cell> findCell(@NotNull final Sheet sheet, @NotNull final String... titles) {
        for (final Row row : sheet) {
            final var titleCell = row.getCell(0);
            if (titleCell.getCellType().equals(CellType.STRING)) {
                final var cellTitle = titleCell.getStringCellValue();
                for (final var title : titles) {
                    if (title.equalsIgnoreCase(cellTitle)) {
                        return Optional.ofNullable(row.getCell(1));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private List<ColumnHeader> findHeaders(@NotNull final Sheet sheet) {
        final var row = findCell(sheet, "#").orElseThrow().getRow();
        final var columnHeaders = new ArrayList<ColumnHeader>();
        for (final var cell : row) {
            if (cell != null && cell.getCellType().equals(CellType.STRING)) {
                final var index = cell.getColumnIndex();
                final var title = cell.getStringCellValue();
                columnHeaders.add(new ColumnHeader(index, title));
            }
        }
        return Collections.unmodifiableList(columnHeaders);
    }

    private Optional<ColumnHeader> findColumn(@NotNull final List<ColumnHeader> columnHeaders, @NotNull final String title) {
        for (final ColumnHeader columnHeader : columnHeaders) {
            if (columnHeader.getTitle().contains(title)) {
                return Optional.of(columnHeader);
            }
        }
        return Optional.empty();
    }

    private Optional<String> getStringFromRow(@NotNull final Row row, @NotNull final ColumnHeader column) {
        final var cell = row.getCell(column.getIndex());
        if (cell != null && cell.getCellType().equals(CellType.STRING)) {
            return Optional.ofNullable(cell.getStringCellValue());
        }
        return Optional.empty();
    }

    private Optional<Date> getDateFromRow(@NotNull final Row row, @NotNull final ColumnHeader column) {
        final var cell = row.getCell(column.getIndex());
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC: return Optional.ofNullable(cell.getDateCellValue());
                case STRING: return getOptionalDateFromString(cell.getStringCellValue());
            }
        }
        return Optional.empty();
    }

    private Optional<Date> getOptionalDateFromString(@Nullable final String value) {
        if (value != null && !value.isBlank()) {
            try {
                return Optional.of(new SimpleDateFormat("MMM dd yyyy HH:mm a", Locale.US).parse(value));
            } catch (final ParseException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public String getWebinarName() {
        return webinarName;
    }

    public String getWebinarUrl() {
        return webinarUrl;
    }

    public synchronized List<BigMarkerRegistration> getRegistrations() {
        if (registrations != null) {
            return registrations;
        }

        final var sheet = workbook.getSheet("registered list");
        final var columnHeaders = findHeaders(sheet);
        final var firstNameColumn = findColumn(columnHeaders, "First Name").orElseThrow();
        final var lastNameColumn = findColumn(columnHeaders, "Last Name").orElseThrow();
        final var emailColumn = findColumn(columnHeaders, "Email").orElseThrow();
        final var registrationDateColumn = findColumn(columnHeaders, "Registration Date").orElseThrow();
        final var timezoneColumn = findColumn(columnHeaders, "Time Zone").orElseThrow();
        final var unsubscribedColumn = findColumn(columnHeaders, "Unsubscribed").orElseThrow();
        final var attendedLiveColumn = findColumn(columnHeaders, "Attended Live").orElseThrow();
        final var firstDataRowIndex = findCell(sheet, "#").orElseThrow().getRowIndex() + 1;
        final var totalRegistered = Integer.parseUnsignedInt(findCell(sheet, "Total Registered").orElseThrow().getStringCellValue());

        final var attendees = new ArrayList<BigMarkerRegistration>();
        for (int rowNum = firstDataRowIndex; rowNum < firstDataRowIndex + totalRegistered; rowNum++) {
            final var row = sheet.getRow(rowNum);
            final var firstName = getStringFromRow(row, firstNameColumn).orElse("");
            final var lastName = getStringFromRow(row, lastNameColumn).orElse("");
            final var email = getStringFromRow(row, emailColumn).orElseThrow();
            final var date = getDateFromRow(row, registrationDateColumn).orElse(null);
            final var timezone = getStringFromRow(row, timezoneColumn).orElse(null);
            final var registrationDate = date == null || timezone == null ? null :
                    ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(timezone));
            final var unsubscribed = getStringFromRow(row, unsubscribedColumn).orElseThrow().equals("Yes");
            final var attendedLive = getStringFromRow(row, attendedLiveColumn).orElseThrow().equals("Yes");
            final var attendee = new BigMarkerRegistration(firstName, lastName, email, registrationDate, unsubscribed, attendedLive);
            attendees.add(attendee);
        }
        registrations = Collections.unmodifiableList(attendees);
        return registrations;
    }

    private Member getOrCreateMember(@NotNull final MemberService memberService,
                                     @NotNull final BigMarkerRegistration registration) {
        final var existingMember = memberService.getByEmail(registration.getEmail());
        if (existingMember.isPresent()) {
            return existingMember.get();
        }

        final var newMember = memberService.newMember();
        newMember.setFirstName(registration.getFirstName());
        newMember.setLastName(registration.getLastName());
        newMember.setEmail(registration.getEmail());
        if (registration.getRegistrationDate() != null) {
            newMember.setRegistrationDate(registration.getRegistrationDate().toLocalDateTime());
        }
        memberService.store(newMember);
        return newMember;
    }

    public void importRegistrations(@NotNull final EventService eventService,
                                    @NotNull final EventMemberService eventMemberService,
                                    @NotNull final MemberService memberService) {
        final var event = eventService.getByWebinarUrl(webinarUrl).orElseThrow(() ->
                new NoSuchElementException(String.format("No event found with webinar URL: %s", webinarUrl)));
        for (final var registration : getRegistrations()) {
            final var member = getOrCreateMember(memberService, registration);
            final var existingRegistration = eventMemberService.get(event.getId(), member.getId());
            if (existingRegistration.isPresent()) {
                existingRegistration.get().setNoShow(registration.isNoShow());
                eventMemberService.store(existingRegistration.get());
            } else {
                final var newRegistration = eventMemberService.newRegistration();
                newRegistration.setEventId(event.getId());
                newRegistration.setMemberId(member.getId());
                if (registration.getRegistrationDate() != null) {
                    newRegistration.setDate(registration.getRegistrationDate().toLocalDateTime());
                }
                newRegistration.setNoShow(registration.isNoShow());
                eventMemberService.store(newRegistration);
            }
        }
    }
}
