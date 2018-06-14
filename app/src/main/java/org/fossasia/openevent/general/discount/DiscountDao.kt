package org.fossasia.openevent.general.discount

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import org.fossasia.openevent.general.event.EventId

@Dao
interface DiscountDao {
    @Insert(onConflict = REPLACE)
    fun insertDiscountCode(discountCode: DiscountCode)

    @Query("DELETE FROM DiscountCode")
    fun deleteAll()

    @Query("SELECT * from DiscountCode WHERE event = :event")
    fun getDiscountCode(event: EventId): Single<DiscountCode>
}