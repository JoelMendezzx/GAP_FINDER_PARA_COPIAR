package com.backend.gapfinder.adapters;

import com.backend.gapfinder.mapper.GoogleEventMapper;
import com.backend.gapfinder.models.ClassBlockModel;
import com.backend.gapfinder.services.GoogleCalendarService;
import com.google.api.services.calendar.model.Event;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

// Adapter del patrón Adapter: adapta GoogleCalendarService (el Adaptee, que devuelve
// eventos de Google) a la interfaz ScheduleSource (el Target, que devuelve ClassBlockModel)
@Component
public class GoogleCalendarScheduleAdapter implements ScheduleSource {

    private final GoogleCalendarService googleCalendarService;

    public GoogleCalendarScheduleAdapter(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    // Convierte cada evento de Google en un bloque de clase y descarta los que no tienen hora
    @Override
    public List<ClassBlockModel> getWeeklyClassBlocks(Long userId) throws Exception {
        List<Event> events = googleCalendarService.getEvents(userId);

        return events.stream()
                .map(GoogleEventMapper::toClassBlock)
                .filter(Objects::nonNull)
                .toList();
    }
}
