package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.data.HomeAsyncLoadingButton
import ru.hryasch.coachnotes.home.data.HomeSimpleButton
import ru.hryasch.coachnotes.home.impl.HomePresenterImpl
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*
import java.util.stream.Collectors

@ExperimentalCoroutinesApi
class HomeFragment : MvpAppCompatFragment(), HomeView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: HomePresenterImpl

    private lateinit var navController: NavController

    // UI
    private lateinit var peopleButton:   HomeAsyncLoadingButton
    private lateinit var groupsButton:   HomeAsyncLoadingButton
    private lateinit var journalsButton: HomeSimpleButton
    private lateinit var todayScheduleDate: TextView

    private var journalPickerDialog: AlertDialog? = null


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        navController = container!!.findNavController()

        val layout = inflater.inflate(R.layout.fragment_home, container, false)

        peopleButton = HomeAsyncLoadingButton
            .setView(layout.findViewById(R.id.homePersonsCard))
            .setOnViewClickListener(View.OnClickListener { navController.navigate(R.id.action_homeFragmentImpl_to_personListFragment) })
            .setCountView(layout.findViewById(R.id.homeTextViewPersonsCount))
            .setLoadingView(layout.findViewById(R.id.homeProgressBarPersonsCount))
            .build()

        groupsButton = HomeAsyncLoadingButton
            .setView(layout.findViewById(R.id.homeGroupsCard))
            .setOnViewClickListener(View.OnClickListener { navController.navigate(R.id.action_homeFragmentImpl_to_groupListFragment) })
            .setCountView(layout.findViewById(R.id.homeTextViewGroupsCount))
            .setLoadingView(layout.findViewById(R.id.homeProgressBarGroupsCount))
            .build()

        journalsButton = HomeSimpleButton
            .setView(layout.findViewById(R.id.homeJournalsCard))
            .setOnViewClickListener(View.OnClickListener { journalPickerDialog?.show() })
            .build()


        val dayOfWeekNames: Array<String> = get(named("daysOfWeekLong_RU"))
        todayScheduleDate = layout.findViewById(R.id.homeTextViewTodayScheduleDate)
        val today = ZonedDateTime.now()
        val todayDayOfWeek = dayOfWeekNames[today.dayOfWeek.value - 1].toLowerCase(Locale.getDefault())
        todayScheduleDate.text = container.context.getString(R.string.home_screen_today_schedule_date, todayDayOfWeek, today.dayOfMonth, today.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()), today.year)

        return layout
    }

    override fun setPeopleCount(count: Int?)
    {
        peopleButton.setCount(count)
    }

    override fun setGroups(groups: List<Group>?)
    {
        updateJournalPickerDialog(groups)
        groupsButton.setCount(groups?.size)
    }

    private fun updateJournalPickerDialog(groups: List<Group>?)
    {
        if (groups == null || groups.isEmpty())
        {
            createNoGroupsDialog()
            return
        }

        val sortedGroups = groups.stream()
                                 .filter { group -> group.membersList.isNotEmpty() }
                                 .sorted()
                                 .collect(Collectors.toList())

        if (sortedGroups.isEmpty()) {
            createNoGroupsDialog()
            return
        }

        val dataArray: Array<String> = Array(sortedGroups.size) { "" }
        for ((i, group) in sortedGroups.withIndex())
        {
            dataArray[i] = "${group.name} (${group.availableAbsoluteAge?.first} - ${group.availableAbsoluteAge?.last} г.р)"
        }

        journalPickerDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите группу")
            .setItems(dataArray) { dialog, position ->
                val action = HomeFragmentDirections.actionHomeFragmentImplToJournalGroupFragment(sortedGroups[position])
                navController.navigate(action)
                dialog.cancel()
            }
            .create()
    }

    private fun createNoGroupsDialog() {
        journalPickerDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Нет групп с учениками")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()

        journalPickerDialog!!.setOnShowListener {
            journalPickerDialog!!.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
        }
    }
}