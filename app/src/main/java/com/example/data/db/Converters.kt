package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.DestinationType
import com.example.data.model.MatchMode
import com.example.data.model.SmtpSecurityType

class Converters {
    @TypeConverter
    fun fromMatchMode(value: MatchMode): String = value.name

    @TypeConverter
    fun toMatchMode(value: String): MatchMode = try {
        MatchMode.valueOf(value)
    } catch (e: Exception) {
        MatchMode.ANY
    }

    @TypeConverter
    fun fromDestinationType(value: DestinationType): String = value.name

    @TypeConverter
    fun toDestinationType(value: String): DestinationType = try {
        DestinationType.valueOf(value)
    } catch (e: Exception) {
        DestinationType.WEBHOOK
    }

    @TypeConverter
    fun fromSmtpSecurityType(value: SmtpSecurityType): String = value.name

    @TypeConverter
    fun toSmtpSecurityType(value: String): SmtpSecurityType = try {
        SmtpSecurityType.valueOf(value)
    } catch (e: Exception) {
        SmtpSecurityType.STARTTLS
    }
}
