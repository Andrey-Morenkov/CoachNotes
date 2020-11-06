package ru.hryasch.coachnotes.fragments.impl

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawegio.kandroid.visible
import com.tiper.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.LoginActivity
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.common.EditCoachBaseParamsElement
import ru.hryasch.coachnotes.common.FieldsCorrectListener
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.CoachData
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.data.HomeScheduleCell
import ru.hryasch.coachnotes.home.impl.HomePresenterImpl
import ru.hryasch.coachnotes.repository.global.GlobalSettings
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale


@ExperimentalCoroutinesApi
class HomeFragment: MvpAppCompatFragment(), HomeView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: HomePresenterImpl

    // UI
    private lateinit var todayScheduleDate: TextView
    private lateinit var scheduleLoading: ProgressBar
    private lateinit var scheduleView: WeekView<HomeScheduleCell>

    // Dialogs
    private lateinit var groupHasNoMembersDialog: AlertDialog

    // Data
    private lateinit var coachFullName: GlobalSettings.Coach.CoachFullName
    private lateinit var coachRole: String
    private var editParamsElement: EditCoachBaseParamsElement? = null
    private val coachRoles: List<String> = get(named("coachRoles"))



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val layout = inflater.inflate(R.layout.fragment_home, container, false)
        todayScheduleDate = layout.findViewById(R.id.homeScheduleTextViewTodayDate)
        scheduleLoading = layout.findViewById(R.id.homeScheduleLoading)
        scheduleView = layout.findViewById(R.id.homeScheduleWeekView)

        setTodayDate()
        tuneScheduleView()
        createGroupHasNoMembersWarningDialog()

        return layout
    }

    override fun setCoachData(coachData: CoachData)
    {
        val (fullName, role) = coachData
        coachFullName = fullName
        coachRole = role

        val coachShowName = "${coachFullName.surname} ${coachFullName.name}"
        homeScreenTextViewCoachName.text = coachShowName
        homeScreenTextViewCoachRole.text = coachRole

        homeScreenTextViewCoachName.setOnLongClickListener {
            showEditCoachBaseParamsDialog()
            true
        }
    }

    override fun setScheduleCells(scheduleCells: List<HomeScheduleCell>?)
    {
        if (scheduleCells == null)
        {
            loadingState()
        }
        else
        {
            showingState(scheduleCells)
        }
    }


    private fun loadingState()
    {
        scheduleLoading.visible = true
        scheduleView.visible = false
    }

    private fun createGroupHasNoMembersWarningDialog()
    {
        groupHasNoMembersDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Невозможно отобразить журнал")
            .setMessage("В группе нет учеников")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun showEditCoachBaseParamsDialog()
    {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_coach_base_params, null)
        val fullName: EditText = dialogView.findViewById(R.id.coachBaseParamEditTextFullName)
        val role: MaterialSpinner = dialogView.findViewById(R.id.coachBaseParamSpinnerRole)
        val customCoachRole: EditText = dialogView.findViewById(R.id.coachBaseParamEditTextCustomRole)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Редактор тренера")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                if (role.selection == coachRoles.indexOf(getString(R.string.coach_role_custom)))
                {
                    presenter.changeCoachInfo(fullName.text.toString(), customCoachRole.text.toString())
                }
                else
                {
                    presenter.changeCoachInfo(fullName.text.toString(), role.selectedItem as String)
                }
                editParamsElement = null
                dialog.cancel()
            }
            .setNegativeButton("Выход") { dialog, _ ->
                editParamsElement = null
                GlobalSettings.Coach.clearLoginData()
                dialog.cancel()
                startActivity(Intent(activity, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                requireActivity().finish()
            }
            .create()
        dialog.show()

        editParamsElement = EditCoachBaseParamsElement(
                                requireContext(),
                                fullName,
                                role,
                                customCoachRole,
                                object : FieldsCorrectListener
                                {
                                    override fun onFieldsCorrect(isCorrect: Boolean)
                                    {
                                        if (isCorrect)
                                        {
                                            with(dialog.getButton(DialogInterface.BUTTON_POSITIVE))
                                            {
                                                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                                                isEnabled = true
                                            }
                                        }
                                        else
                                        {
                                            with(dialog.getButton(DialogInterface.BUTTON_POSITIVE))
                                            {
                                                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))
                                                isEnabled = false
                                            }
                                        }
                                    }
                                },
                                coachRole,
                                coachFullName.toString())

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
    }

    private fun showingState(scheduleCells: List<HomeScheduleCell>)
    {
        scheduleView.submit(scheduleCells)
        scheduleLoading.visible = false
        scheduleView.visible = true
    }

    private fun tuneScheduleView()
    {
        val now = Calendar.getInstance()
        val min = now.clone() as Calendar
        min.set(Calendar.DAY_OF_MONTH, 1)
        val max = now.clone() as Calendar
        max.set(Calendar.DAY_OF_MONTH, max.getActualMaximum(Calendar.DAY_OF_MONTH))

        scheduleView.minDate = min
        scheduleView.maxDate = max
        scheduleView.setOnEventClickListener { data, _ ->
            (requireActivity() as MainActivity).navigateToJournalFragment(data.group)
        }
    }

    private fun setTodayDate()
    {
        val dayOfWeekNames: Array<String> = get(named("daysOfWeekLong_RU"))
        val today = ZonedDateTime.now()
        val todayDayOfWeek = dayOfWeekNames[today.dayOfWeek.value - 1].toLowerCase(Locale.getDefault())
        todayScheduleDate.text = getString(R.string.home_screen_today_schedule_date, todayDayOfWeek, today.dayOfMonth, today.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()), today.year)

        todayScheduleDate.setOnClickListener {
            scheduleView.goToToday()
            scheduleView.goToCurrentTime()
        }
    }
}

data class ScheduleDayInfo(
    val group: Group,
    val startTime: Calendar,
    val endTime: Calendar
)
{
    override fun toString(): String
    {
        return "Group ${group.name} (${group.id}), start = ${startTime.get(Calendar.HOUR_OF_DAY)}:${startTime.get(Calendar.MINUTE)}, end = ${endTime.get(Calendar.HOUR_OF_DAY)}:${endTime.get(Calendar.MINUTE)}"
    }
}