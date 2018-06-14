package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.auth.DiscountCode
import org.fossasia.openevent.general.discount.DiscountService
import timber.log.Timber

class EventDetailsViewModel(private val eventService: EventService, private val discountService: DiscountService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()
    val discountCode = MutableLiveData<DiscountCode>()
    val error = MutableLiveData<String>()

    fun loadDiscountCode(id : Long) {
        val event = EventId(id)
        compositeDisposable.add(discountService.getDiscountCode(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    discountCode.value = it
                },{
                    error.value = "Error Loading DiscountCode..."
                    Timber.e(it, "Error loading discount code %d",id)
                }))
    }

    fun loadEvent(id : Long) {
        if (id.equals(-1)) {
            error.value = "Error fetching event"
            return
        }
        compositeDisposable.add(eventService.getEvent(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally({
                    progress.value = false
                }).subscribe({
                    event.value = it
                    loadDiscountCode(id)
                }, {
                    Timber.e(it, "Error fetching event %d",id)
                    error.value = "Error fetching event"
                }))
    }

    fun loadMap(event: Event): String{
        //location handling
        val mapUrlInitial = "https://maps.googleapis.com/maps/api/staticmap?center="
        val mapUrlProperties = "&zoom=12&size=1200x390&markers=color:red%7C"
        val mapUrlMapType = "&markers=size:mid&maptype=roadmap"

        val latLong: String = "" +event.latitude + "," + event.longitude

        return mapUrlInitial + latLong + mapUrlProperties + latLong + mapUrlMapType
    }

    fun loadMapUrl(event: Event): String{
        //load map url
        return "geo:<"+event.latitude+">,<"+event.longitude+">?q=<"+event.latitude+">,<"+event.longitude+">"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}