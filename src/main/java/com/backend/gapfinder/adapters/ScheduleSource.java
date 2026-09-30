package com.backend.gapfinder.adapters;

import com.backend.gapfinder.models.ClassBlockModel;
import java.util.List;

// Target del patrón Adapter: fuente de horarios que la app entiende,
// independiente del proveedor externo (Google, Outlook, archivo, etc.)
public interface ScheduleSource {

    // Devuelve los bloques de clase de la semana actual (lunes a sábado) del usuario
    List<ClassBlockModel> getWeeklyClassBlocks(Long userId) throws Exception;
}
