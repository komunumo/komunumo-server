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

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.entity.Member;
import org.komunumo.data.service.DatabaseService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.komunumo.util.WorkbookUtil.findCell;
import static org.komunumo.util.WorkbookUtil.findColumn;
import static org.komunumo.util.WorkbookUtil.getColumnHeaders;
import static org.komunumo.util.WorkbookUtil.getDateFromRow;
import static org.komunumo.util.WorkbookUtil.getStringFromRow;

public final class BigMarkerReport {

    private final Workbook workbook;

    private final String webinarUrl;

    private List<BigMarkerRegistration> bigMarkerRegistrations = null;

    public BigMarkerReport(@NotNull final InputStream inputStream) throws IOException {
        this.workbook = new XSSFWorkbook(inputStream);

        final var summary = workbook.getSheet("summary");
        webinarUrl = findCell(summary, "URL").orElseThrow().getStringCellValue();
    }

    public synchronized List<BigMarkerRegistration> getBigMarkerRegistrations() {
        if (bigMarkerRegistrations != null) {
            return bigMarkerRegistrations;
        }

        final var sheet = workbook.getSheet("registered list");
        final var columnHeaders = getColumnHeaders(findCell(sheet, "#").orElseThrow().getRow());
        final var firstNameColumn = findColumn(columnHeaders, "First Name").orElseThrow();
        final var lastNameColumn = findColumn(columnHeaders, "Last Name").orElseThrow();
        final var emailColumn = findColumn(columnHeaders, "Email").orElseThrow();
        final var registrationDateColumn = findColumn(columnHeaders, "Registration Date").orElseThrow();
        final var timezoneColumn = findColumn(columnHeaders, "Time Zone").orElseThrow();
        final var unsubscribedColumn = findColumn(columnHeaders, "Unsubscribed").orElseThrow();
        final var attendedLiveColumn = findColumn(columnHeaders, "Attended Live").orElseThrow();
        final var membershipColumn = findColumn(columnHeaders, "Membership");
        final var firstDataRowIndex = findCell(sheet, "#").orElseThrow().getRowIndex() + 1;
        final var totalRegistered = Integer.parseUnsignedInt(findCell(sheet, "Total Registered").orElseThrow().getStringCellValue());

        final var attendees = new ArrayList<BigMarkerRegistration>();
        for (int rowNum = firstDataRowIndex; rowNum < firstDataRowIndex + totalRegistered; rowNum++) {
            final var row = sheet.getRow(rowNum);
            final var firstName = getStringFromRow(row, firstNameColumn).orElse("");
            final var lastName = getStringFromRow(row, lastNameColumn).orElse("");
            final var email = getStringFromRow(row, emailColumn).orElse(null);
            final var date = getDateFromRow(row, registrationDateColumn).orElse(null);
            final var timezone = getStringFromRow(row, timezoneColumn).orElse(null);
            final var registrationDate = date == null || timezone == null ? null
                    : ZonedDateTime.ofInstant(date.toInstant(), !timezone.isBlank() ? ZoneId.of(timezone) : ZoneId.systemDefault());
            final var unsubscribed = getStringFromRow(row, unsubscribedColumn).orElseThrow().equals("Yes");
            final var attendedLive = getStringFromRow(row, attendedLiveColumn).orElseThrow().equals("Yes");
            final var membership = membershipColumn.map(columnHeader -> getStringFromRow(row, columnHeader).orElseThrow()).orElse(null);
            final var attendee = new BigMarkerRegistration(firstName, lastName, email, registrationDate, unsubscribed, attendedLive, membership);
            attendees.add(attendee);
        }
        bigMarkerRegistrations = Collections.unmodifiableList(attendees);
        return bigMarkerRegistrations;
    }

    private Member getOrCreateMember(@NotNull final DatabaseService databaseService,
                                     @NotNull final BigMarkerRegistration registration) {
        final Optional<Member> existingMember = registration.email() == null ? Optional.empty()
                : databaseService.getMemberByEmail(registration.email());
        if (existingMember.isPresent()) {
            return existingMember.get();
        }

        final var newMember = databaseService.newMember();
        newMember.setFirstName(registration.firstName());
        newMember.setLastName(registration.lastName());
        newMember.setEmail(registration.email());
        if (registration.registrationDate() != null) {
            newMember.setRegistrationDate(registration.registrationDate().toLocalDateTime());
        }
        final var membership = registration.membership();
        if (membership != null && !membership.isBlank() && !membership.toLowerCase(Locale.getDefault()).contains("none")) {
            newMember.setComment("Registered at BigMarker\nMembership: " + registration.membership());
        } else {
            newMember.setComment("Registered at BigMarker");
        }
        newMember.store();
        return newMember;
    }

    public void importRegistrations(@NotNull final DatabaseService databaseService) {
        final var event = databaseService.getEventByWebinarUrl(webinarUrl).orElseThrow(() ->
                new NoSuchElementException(String.format("No event found with webinar URL: %s", webinarUrl)));
        for (final var bigMarkerRegistration : getBigMarkerRegistrations()) {
            final var noShow = bigMarkerRegistration.noShow();
            final var member = getOrCreateMember(databaseService, bigMarkerRegistration);
            final var existingRegistration = databaseService.getRegistration(event.getId(), member.getId());
            if (existingRegistration.isPresent()) {
                final var registration = existingRegistration.get();
                databaseService.updateNoShow(registration, noShow);
            } else {
                final var date = bigMarkerRegistration.registrationDate() != null
                        ? bigMarkerRegistration.registrationDate().toLocalDateTime() : LocalDateTime.now();
                databaseService.registerForEvent(event, member, date, "BigMarker", noShow, false);
            }
        }
    }
}
