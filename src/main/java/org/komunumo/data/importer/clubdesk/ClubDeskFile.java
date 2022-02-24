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

package org.komunumo.data.importer.clubdesk;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.DatabaseService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.komunumo.util.WorkbookUtil.findColumn;
import static org.komunumo.util.WorkbookUtil.getColumnHeaders;
import static org.komunumo.util.WorkbookUtil.getLocalDateFromRow;
import static org.komunumo.util.WorkbookUtil.getLongFromRow;
import static org.komunumo.util.WorkbookUtil.getStringFromRow;

public final class ClubDeskFile {

    private final Workbook workbook;

    private List<ClubDeskMember> members = null;

    public ClubDeskFile(@NotNull final InputStream inputStream) throws IOException {
        this.workbook = new XSSFWorkbook(inputStream);
    }

    public List<ClubDeskMember> getMembers() {
        if (members != null) {
            return members;
        }
        final var sheet = workbook.getSheetAt(0);
        final var columnHeaders = getColumnHeaders(sheet.getRow(0));
        final var membershipBeginDateColumn = findColumn(columnHeaders, "Eintritt").orElseThrow();
        final var membershipEndDateColumn = findColumn(columnHeaders, "Austritt").orElseThrow();
        final var membershipIdColumn = findColumn(columnHeaders, "M-Nr.").orElseThrow();
        final var firstNameColumn = findColumn(columnHeaders, "Vorname").orElseThrow();
        final var lastNameColumn = findColumn(columnHeaders, "Nachname").orElseThrow();
        final var companyColumn = findColumn(columnHeaders, "Firma").orElseThrow();
        final var emailColumn = findColumn(columnHeaders, "E-Mail").orElseThrow();
        final var addressColumn = findColumn(columnHeaders, "Adresse").orElseThrow();
        final var zipCodeColumn = findColumn(columnHeaders, "PLZ").orElseThrow();
        final var cityColumn = findColumn(columnHeaders, "Ort").orElseThrow();
        final var commentColumn = findColumn(columnHeaders, "Bemerkungen").orElseThrow();

        final var clubDeskMembers = new ArrayList<ClubDeskMember>();
        for (final Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue; // header row
            }
            final var membershipBeginDate = getLocalDateFromRow(row, membershipBeginDateColumn).orElse(null);
            final var membershipEndDate = getLocalDateFromRow(row, membershipEndDateColumn).orElse(null);
            final var membershipId = getLongFromRow(row, membershipIdColumn).orElse(null);
            final var firstName = getStringFromRow(row, firstNameColumn).orElse("");
            final var lastName = getStringFromRow(row, lastNameColumn).orElse("");
            final var company = getStringFromRow(row, companyColumn).orElse("");
            final var email = getStringFromRow(row, emailColumn).orElse("");
            final var address = getStringFromRow(row, addressColumn).orElse("");
            final var zipCode = getStringFromRow(row, zipCodeColumn).orElse("").replaceFirst("\\.0$", "");
            final var city = getStringFromRow(row, cityColumn).orElse("");
            final var comment = getStringFromRow(row, commentColumn).orElse("");
            final var clubDeskMember = new ClubDeskMember(
                    membershipBeginDate, membershipEndDate, membershipId,
                    firstName, lastName, company, email,
                    address, zipCode, city, comment);
            clubDeskMembers.add(clubDeskMember);
        }
        members = Collections.unmodifiableList(clubDeskMembers);
        return members;
    }

    public void importMembers(@NotNull final DatabaseService databaseService) {
        for (final var clubDeskMember : getMembers()) {
            final var email = clubDeskMember.email();
            final var member = databaseService.getMemberByEmail(email).orElse(databaseService.newMember());
            member.setMembershipBegin(clubDeskMember.membershipBeginDate());
            member.setMembershipEnd(clubDeskMember.membershipEndDate());
            member.setMembershipId(clubDeskMember.membershipId());
            member.setFirstName(clubDeskMember.firstName());
            member.setLastName(clubDeskMember.lastName());
            member.setCompany(clubDeskMember.company());
            member.setEmail(clubDeskMember.email());
            member.setAddress(clubDeskMember.address());
            member.setZipCode(clubDeskMember.zipCode());
            member.setCity(clubDeskMember.city());
            member.setComment(clubDeskMember.comment());
            member.store();
        }
    }
}
