package org.fossasia.openevent.general

import android.arch.persistence.room.TypeConverter
import org.fossasia.openevent.general.event.EventId


class Converter {

    @TypeConverter
    fun fromEvent(eventId: EventId): Long {
        return eventId.id
    }

    @TypeConverter
    fun toEvent(id: Long): EventId {
        val eventId = EventId(id)
        return eventId
    }
}