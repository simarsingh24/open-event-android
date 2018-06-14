package org.fossasia.openevent.general.discount

import android.arch.persistence.room.EmptyResultSetException
import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import org.fossasia.openevent.general.event.EventId

class DiscountService(private val discountApi: DiscountApi, private val discountDao: DiscountDao) {

    fun getDiscountCode(event: EventId): Single<DiscountCode> {
        val singleDiscount = discountDao.getDiscountCode(event)
        return singleDiscount.onErrorResumeNext({
            if (it is EmptyResultSetException) {
                discountApi.getDiscountCodes(event.id)
            }
            null
        })
    }
}