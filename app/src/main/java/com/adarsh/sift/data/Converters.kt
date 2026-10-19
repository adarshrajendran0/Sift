package com.adarsh.sift.data

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromStatus(status: Status?): String? {
        return status?.name
    }

    @TypeConverter
    fun toStatus(name: String?): Status? {
        return name?.let { Status.valueOf(it) }
    }

    @TypeConverter
    fun fromTimeEstimate(estimate: TimeEstimate?): String? {
        return estimate?.name
    }

    @TypeConverter
    fun toTimeEstimate(name: String?): TimeEstimate? {
        return name?.let { TimeEstimate.valueOf(it) }
    }

    @TypeConverter
    fun fromSource(source: Source?): String? {
        return source?.name
    }

    @TypeConverter
    fun toSource(name: String?): Source? {
        return name?.let { Source.valueOf(it) }
    }
}
