package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.*
import kotlinx.android.synthetic.main.content_event.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.DiscountCode
import org.fossasia.openevent.general.ticket.TicketsFragment
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class EventDetailsFragment : Fragment() {
    val EVENT_ID = "EVENT_ID"
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private lateinit var rootView: View
    private var eventId: Long = -1
    private lateinit var eventShare: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        eventViewModel.event.observe(this, Observer {
            it?.let {
                loadEvent(it)
                eventShare = it
            }
            loadTicketFragment()
            Timber.d("Fetched events of id %d", eventId)
        })

        eventViewModel.discountCode.observe(this, Observer {
            it?.let {
                loadDiscountCode(it)
            }
        })

        eventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventViewModel.loadEvent(eventId)

        return rootView
    }

    private fun loadDiscountCode(discountCode: DiscountCode){
        setTextField(discount_code_text_view,discountCode.code)
        setTextField(discount_url,discountCode.discountUrl)
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.event_name.text = event.name
        rootView.event_organiser_name.text = "by " + event.organizerName.nullToEmpty()
        setTextField(rootView.event_description, event.description)
        setTextField(rootView.event_organiser_description, event.organizerDescription)
        rootView.event_location_text_view.text = event.locationName

        rootView.starts_on.text = "${startsAt.dayOfMonth} ${startsAt.month} ${startsAt.year}"
        rootView.ends_on.text = "${endsAt.dayOfMonth} ${endsAt.month} ${endsAt.year}"

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(rootView.logo)
        }

        //load location to map
        rootView.location_under_map.text = event.locationName
        val mapClickListener = View.OnClickListener { startMap(event) }
        if (event.locationName != null) {
            rootView.location_under_map.visibility = View.VISIBLE
            rootView.image_map.visibility = View.VISIBLE
            rootView.image_map.setOnClickListener(mapClickListener)
            rootView.event_location_text_view.setOnClickListener(mapClickListener)
        }

        Picasso.get()
                .load(eventViewModel.loadMap(event))
                .placeholder(R.drawable.ic_map_black_24dp)
                .into(rootView.image_map)

        //Add event to Calendar
        val dateClickListener = View.OnClickListener { startCalendar(event) }
        rootView.starts_on.setOnClickListener(dateClickListener)
        rootView.ends_on.setOnClickListener(dateClickListener)

    }

    override fun onDestroyView() {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.add_to_calendar -> {
                //Add event to Calendar
                startCalendar(eventShare)
                return true
            }
            R.id.event_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(eventShare))
                sendIntent.type = "text/plain"
                rootView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        if (value.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = value
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.setGroupVisible(R.id.event_menu, true)
        super.onPrepareOptionsMenu(menu)
    }

    private fun startCalendar(event: Event){
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, EventUtils.getTimeInMilliSeconds(event.startsAt))
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, EventUtils.getTimeInMilliSeconds(event.endsAt))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val inflaterMenu = activity?.menuInflater
        inflaterMenu?.inflate(R.menu.event_details, menu)
    }
  
    private fun loadTicketFragment(){
        //Initialise Ticket Fragment
        val ticketFragment = TicketsFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        ticketFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.frameContainer, ticketFragment).commit()
    }

    private fun startMap(event: Event) {
        // start map intent
        val mapUrl = eventViewModel.loadMapUrl(event)
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        if (mapIntent.resolveActivity(activity?.packageManager) != null) {
            startActivity(mapIntent)
        }
    }
}