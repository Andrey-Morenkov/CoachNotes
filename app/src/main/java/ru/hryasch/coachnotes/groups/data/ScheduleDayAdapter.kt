package ru.hryasch.coachnotes.groups.data

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
import com.google.android.material.textfield.TextInputLayout
import com.pawegio.kandroid.e
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay

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
    private val checkBox: CheckBox = itemView.findViewById(R.id.group_schedule_day_checkbox)
    private val name: TextView = itemView.findViewById(R.id.group_schedule_day_day_name)

    private lateinit var timeStartPicker: TimePickerDialog
    private val timeStartTimePlace: TextInputEditText = itemView.findViewById(R.id.group_schedule_day_time_from_time_place)

    private lateinit var timeFinishPicker: TimePickerDialog
    private val timeFinishTimePlace: TextInputEditText = itemView.findViewById(R.id.group_schedule_day_time_until_time_place)

    private var isChecked: Boolean = false
    private var startHour:    Int = 12
    private var startMinute:  Int = 0
    private var finishHour:   Int = 13
    private var finishMinute: Int = 0

    private lateinit var scheduleDay: ScheduleDay

    init
    {
        initLayout()
    }

    fun bind(scheduleDay: ScheduleDay)
    {
        this.scheduleDay = scheduleDay

        name.text = scheduleDay.dayName
        e("bind scheduleDay: $scheduleDay")

        if (scheduleDay.startTime.isNotBlank())
        {
            val startTime = scheduleDay.startTime.split(":")
            startHour   = startTime[0].toInt()
            startMinute = startTime[1].toInt()

            val finishTime = scheduleDay.endTime.split(":")
            finishHour   = finishTime[0].toInt()
            finishMinute = finishTime[1].toInt()

            isChecked = true
            selectedState()
        }

        setStartTimeString()
        timeStartPicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener
        { _, hourOfDay, minute ->
            startHour = hourOfDay
            startMinute = minute
            setStartTimeString()
            scheduleDay.startTime = timeStartTimePlace.text.toString()
        }, startHour, startMinute, true)

        setFinishTimeString()
        timeFinishPicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener
        { _, hourOfDay, minute ->
            finishHour = hourOfDay
            finishMinute = minute
            setFinishTimeString()
            scheduleDay.endTime = timeFinishTimePlace.text.toString()
        }, finishHour, finishMinute, true)
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
        var timeString = ""
        timeString =
            if (startHour < 10)
            {
                "0$startHour"
            }
            else
            {
                "$startHour"
            }

        timeString += ":"

        if (startMinute < 10)
        {
            timeString += "0$startMinute"
        }
        else
        {
            timeString += startMinute
        }

        timeStartTimePlace.setText(timeString)
    }

    private fun setFinishTimeString()
    {
        var timeString = ""
        timeString =
            if (finishHour < 10)
            {
                "0$finishHour"
            }
            else
            {
                "$finishHour"
            }

        timeString += ":"

        if (finishMinute < 10)
        {
            timeString += "0$finishMinute"
        }
        else
        {
            timeString += finishMinute
        }

        timeFinishTimePlace.setText(timeString)
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
            scheduleDay.startTime = ""
            scheduleDay.endTime = ""
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
            scheduleDay.startTime = timeStartTimePlace.text.toString()
            scheduleDay.endTime = timeFinishTimePlace.text.toString()
        }
    }
}