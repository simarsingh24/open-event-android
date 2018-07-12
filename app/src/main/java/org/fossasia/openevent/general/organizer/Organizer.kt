package org.fossasia.openevent.general.organizer

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId

@Type("user")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["event"], onDelete = ForeignKey.CASCADE))])
data class Organizer(
        @Id(IntegerIdHandler::class)
        @PrimaryKey
        val id: Int,
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val contact: String? = null,
        val details: String? = null,
        val thumbnailImageUrl: String? = null,
        val iconImageUrl: String? = null,
        val smallImageUrl: String? = null,
        val avatarUrl: String? = null,
        val facebookUrl: String? = null,
        val twitterUrl: String? = null,
        val instagramUrl: String? = null,
        val googlePlusUrl: String? = null,
        val originalImageUrl: String? = null,

        val isVerified: Boolean = false,
        val isAdmin: Boolean,
        val isSuperAdmin: Boolean,
        val createdAt: String? = null,
        val lastAccessedAt: String? = null,
        val deletedAt: String? = null,
        @ColumnInfo(index = true)
        @Relationship("event")
        var event: EventId? = null

)