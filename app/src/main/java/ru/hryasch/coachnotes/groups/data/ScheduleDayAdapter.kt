package ru.hryasch.coachnotes.groups.data

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.e
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

class ScheduleDayAdapter(private val scheduleDayList: List<ScheduleDay>, private val context: Context): RecyclerView.Adapter<ScheduleDayViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleDayViewHolder
    {
        return ScheduleDayViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.preview_schedule_day, parent, false), context)
    }

    override fun getItemCount(): Int
    {
        return scheduleDayList.size
    }

    override fun onBindViewHolder(holder: ScheduleDayViewHolder, position: Int)
    {
        holder.bind(scheduleDayList[position])
    }
}



class ScheduleDayViewHolder(itemView: View, val context: Context): RecyclerView.ViewHolder(itemView)
{
    // Data
    private lateinit var scheduleDay: ScheduleDay

    // UI
    private val checkBox: CheckBox = itemView.findViewById(R.id.group_schedule_day_checkbox)
    private val name: TextView = itemView.findViewById(R.id.group_schedule_day_day_name)
    private val timeStartTimePlace: TextInputEditText = itemView.findViewById(R.id.group_schedule_day_time_from_time_place)
    private val timeFinishTimePlace: TextInputEditText = itemView.findViewById(R.id.group_schedule_day_time_until_time_place)
    private lateinit var timeStartPicker: TimePickerDialog
    private lateinit var timeFinishPicker: TimePickerDialog

    // Flags
    private var isChecked: Boolean = false


    private val timeStartCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
    }
    private val timeFinishCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 13)
        set(Calendar.MINUTE, 0)
    }

    companion object
    {
        @SuppressLint("ConstantLocale")
        val defaultTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    }



    init
    {
        initLayout()
    }

    fun bind(scheduleDay: ScheduleDay)
    {
        this.scheduleDay = scheduleDay

        name.text = scheduleDay.dayName
        e("bind scheduleDay: $scheduleDay")

        if (scheduleDay.isNotBlank())
        {
            with(timeStartCalendar)
            {
                set(Calendar.HOUR_OF_DAY, scheduleDay.startTime!!.hour)
                set(Calendar.MINUTE, scheduleDay.startTime!!.minute)
            }

            with(timeFinishCalendar)
            {
                set(Calendar.HOUR_OF_DAY, scheduleDay.endTime!!.hour)
                set(Calendar.MINUTE, scheduleDay.endTime!!.minute)
            }

            isChecked = true
            selectedState()
        }

        setStartTimeString()
        timeStartPicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener
        { _, hourOfDay, minute ->
            with(timeStartCalendar)
            {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            setStartTimeString()
            scheduleDay.startTime = LocalTime.of(hourOfDay, minute)
        }, timeStartCalendar.get(Calendar.HOUR_OF_DAY), timeStartCalendar.get(Calendar.MINUTE), true)

        setFinishTimeString()
        timeFinishPicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener
        { _, hourOfDay, minute ->
            with(timeFinishCalendar)
            {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            setFinishTimeString()
            scheduleDay.endTime = LocalTime.of(hourOfDay, minute)
        }, timeFinishCalendar.get(Calendar.HOUR_OF_DAY), timeFinishCalendar.get(Calendar.MINUTE), true)
    }

    private fun initLayout()
    {
        itemView.setOnClickListener {
            isChecked = !isChecked
            if (isChecked)
            {
                selectedState()
            }
            else
            {
                unselectedState()
            }
        }

        unselectedState()
    }

    private fun setStartTimeString()
    {
        timeStartTimePlace.setText(defaultTimeFormatter.format(timeStartCalendar.time))
    }

    private fun setFinishTimeString()
    {
        timeFinishTimePlace.setText(defaultTimeFormatter.format(timeFinishCalendar.time))
    }

    private fun unselectedState()
    {
        checkBox.isChecked = false
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorScheduleDayUnselectedText))
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorScheduleDayUnselected))
        name.setTextColor(ContextCompat.getColor(context, R.color.colorScheduleDayUnselectedText))
        timeStartTimePlace.setTextColor(ContextCompat.getColor(context, R.color.colorScheduleDayUnselectedText))
        timeFinishTimePlace.setTextColor(ContextCompat.getColor(context, R.color.colorScheduleDayUnselectedText))

        timeStartTimePlace.isClickable = false
        timeFinishTimePlace.isClickable = false
        timeStartTimePlace.isFocusable = false
        timeFinishTimePlace.isFocusable = false
        timeStartTimePlace.setOnClickListener(null)
        timeFinishTimePlace.setOnClickListener(null)

        if (::scheduleDay.isInitialized)
        {
            scheduleDay.startTime = null
            scheduleDay.endTime = null
        }
    }

    private fun selectedState()
    {
        checkBox.isChecked = true
        checkBox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorText))
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
        name.setTextColor(ContextCompat.getColor(context, R.color.colorText))
        timeStartTimePlace.setTextColor(ContextCompat.getColor(context, R.color.colorText))
        timeFinishTimePlace.setTextColor(ContextCompat.getColor(context, R.color.colorText))

        timeStartTimePlace.isClickable = true
        timeFinishTimePlace.isClickable = true
        timeStartTimePlace.isFocusable = true
        timeFinishTimePlace.isFocusable = true
        timeStartTimePlace.setOnClickListener {
            timeStartPicker.show()
        }

        timeFinishTimePlace.setOnClickListener {
            timeFinishPicker.show()
        }

        if (::scheduleDay.isInitialized)
        {
            scheduleDay.startTime = LocalTime.of(timeStartCalendar.get(Calendar.HOUR_OF_DAY), timeStartCalendar.get(Calendar.MINUTE))
            scheduleDay.endTime   = LocalTime.of(timeFinishCalendar.get(Calendar.HOUR_OF_DAY), timeFinishCalendar.get(Calendar.MINUTE))
        }
    }
}