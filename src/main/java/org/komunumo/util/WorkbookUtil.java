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

package org.komunumo.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.importer.ColumnHeader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.komunumo.util.DateUtil.dateToLocalDate;

public class WorkbookUtil {

    public static List<ColumnHeader> getColumnHeaders(@NotNull final Row row) {
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

    public static Optional<Cell> findCell(@NotNull final Sheet sheet,
                                          @NotNull final String... titles) {
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

    public static Optional<ColumnHeader> findColumn(@NotNull final List<ColumnHeader> columnHeaders,
                                                    @NotNull final String title) {
        for (final ColumnHeader columnHeader : columnHeaders) {
            if (columnHeader.title().contains(title)) {
                return Optional.of(columnHeader);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getStringFromRow(@NotNull final Row row,
                                                    @NotNull final ColumnHeader column) {
        final var cell = row.getCell(column.index());
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING: return Optional.ofNullable(cell.getStringCellValue());
                case BLANK: return Optional.of("");
                case NUMERIC: return Optional.of(Double.toString(cell.getNumericCellValue()));
                case BOOLEAN: return Optional.of(Boolean.toString(cell.getBooleanCellValue()));
                case ERROR: throw new IllegalStateException("Error in cell: code + " + cell.getErrorCellValue());
            }
        }
        return Optional.empty();
    }

    public static Optional<Date> getDateFromRow(@NotNull final Row row,
                                                @NotNull final ColumnHeader column) {
        final var cell = row.getCell(column.index());
        if (cell != null) {
            return switch (cell.getCellType()) {
                case NUMERIC -> Optional.ofNullable(cell.getDateCellValue());
                case STRING -> getOptionalDateFromString(cell.getStringCellValue());
                default -> throw new IllegalStateException("Unexpected date cell type: " + cell.getCellType());
            };
        }
        return Optional.empty();
    }

    public static Optional<LocalDate> getLocalDateFromRow(@NotNull final Row row,
                                                          @NotNull final ColumnHeader column) {
        return Optional.ofNullable(
                dateToLocalDate(getDateFromRow(row, column)
                        .orElse(null)));
    }

    public static Optional<Long> getLongFromRow(@NotNull final Row row,
                                                   @NotNull final ColumnHeader column) {
        final var cell = row.getCell(column.index());
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC: return Optional.of((long) cell.getNumericCellValue());
                case STRING:
                    final var value = cell.getStringCellValue();
                    if (value != null && !value.isBlank()) {
                        return Optional.of(Long.parseLong(value));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected date cell type: " + cell.getCellType());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("java:S108")
    private static Optional<Date> getOptionalDateFromString(@Nullable final String value) {
        if (value != null && !value.isBlank()) {
            final var datePatterns = List.of("MMM dd yyyy HH:mm a", "dd.MM.yyyy");
            for (final var datePattern : datePatterns) {
                try {
                    return Optional.of(new SimpleDateFormat(datePattern, Locale.US).parse(value));
                } catch (final ParseException ignored) { }
            }
        }
        return Optional.empty();
    }

    private WorkbookUtil() {
        throw new IllegalStateException("Utility class");
    }

}
