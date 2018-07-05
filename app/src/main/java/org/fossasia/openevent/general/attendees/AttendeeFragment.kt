package org.fossasia.openevent.general.attendees

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_attendee.*
import kotlinx.android.synthetic.main.fragment_attendee.view.*
import org.fossasia.openevent.general.AuthActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.fossasia.openevent.general.ticket.TicketId
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel


class AttendeeFragment : Fragment() {

    private lateinit var rootView: View
    private var id: Long = -1
    private val attendeeFragmentViewModel by viewModel<AttendeeViewModel>()
    private lateinit var eventId: EventId
    private var ticketIdAndQty = ArrayList<Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
            eventId = EventId(id)
            ticketIdAndQty = bundle.getSerializable(TICKET_ID_AND_QTY) as ArrayList<Pair<Int, Int>>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_attendee, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Attendee Details"
        setHasOptionsMenu(true)

        attendeeFragmentViewModel.loadEvent(id)

        if (!attendeeFragmentViewModel.isLoggedIn()) {
            redirectToLogin()
            Toast.makeText(context, "You need to log in first!", Toast.LENGTH_LONG).show()
        }

        attendeeFragmentViewModel.message.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        attendeeFragmentViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBarAttendee, it) }
        })

        attendeeFragmentViewModel.event.observe(this, Observer {
            it?.let { loadEventDetails(it) }
        })

        rootView.register.setOnClickListener {
            ticketIdAndQty.map {
                if (it.second > 0) {
                    val attendee = Attendee(id = attendeeFragmentViewModel.getId(),
                            firstname = firstName.text.toString(),
                            lastname = lastName.text.toString(),
                            email = email.text.toString(),
                            ticket = TicketId(it.first.toLong()),
                            event = eventId)

                    attendeeFragmentViewModel.createAttendee(attendee)
                }
            }
        }

        return rootView
    }

    private fun redirectToLogin() {
        startActivity(Intent(activity, AuthActivity::class.java))
    }

    private fun loadEventDetails(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)

        rootView.eventName.text = "${event.name} - ${EventUtils.getFormattedDate(startsAt)}"
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
                .append(" - ")
                .append(EventUtils.getFormattedDate(endsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}