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

package org.komunumo.ui.view.website.events;

import org.junit.jupiter.api.Test;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.DatabaseService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventPreviewFilterTest {

    @Test
    void dontForwardUnpublishedEvents() throws ServletException, IOException {
        final var event = mock(Event.class);
        when(event.getPublished()).thenReturn(false);

        final var databaseServiceMock = mock(DatabaseService.class);
        when(databaseServiceMock.getEventByUrl(anyString(), any(Year.class), anyString())).thenReturn(Optional.of(event));

        final var request = mock(HttpServletRequest.class);
        when(request.getParameter(eq("preview"))).thenReturn("12345678");
        when(request.getRequestURI()).thenReturn("/event/online/2265/test-event");

        final var response = mock(HttpServletResponse.class);
        final var chain = mock(FilterChain.class);

        final var filter = new EventPreviewFilter(databaseServiceMock);
        filter.doFilter(request, response, chain);

        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setHeader(anyString(), anyString());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void onlyForwardPublishedEvents() throws ServletException, IOException {
        final var event = mock(Event.class);
        when(event.getPublished()).thenReturn(true);
        when(event.getCompleteEventUrl()).thenReturn("/event/online/2269/test-event");

        final var databaseServiceMock = mock(DatabaseService.class);
        when(databaseServiceMock.getEventByUrl(anyString(), any(Year.class), anyString())).thenReturn(Optional.of(event));

        final var request = mock(HttpServletRequest.class);
        when(request.getParameter(eq("preview"))).thenReturn("12345678");
        when(request.getRequestURI()).thenReturn("/event/online/2265/test-event");

        final var response = mock(HttpServletResponse.class);
        final var chain = mock(FilterChain.class);

        final var filter = new EventPreviewFilter(databaseServiceMock);
        filter.doFilter(request, response, chain);

        verify(response, times(1)).setStatus(anyInt());
        verify(response, times(1)).setHeader("Location", "/event/online/2269/test-event");
        verify(chain, never()).doFilter(request, response);
    }

}
