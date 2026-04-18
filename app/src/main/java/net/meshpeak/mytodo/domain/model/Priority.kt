package net.meshpeak.mytodo.domain.model

import androidx.annotation.StringRes
import net.meshpeak.mytodo.R

sealed interface Priority {
    val rank: Int

    @get:StringRes
    val labelRes: Int

    data object Asap : Priority {
        override val rank: Int = 0
        override val labelRes: Int = R.string.priority_asap
    }

    data object Today : Priority {
        override val rank: Int = 1
        override val labelRes: Int = R.string.priority_today
    }

    data object Tomorrow : Priority {
        override val rank: Int = 2
        override val labelRes: Int = R.string.priority_tomorrow
    }

    data object ThisWeek : Priority {
        override val rank: Int = 3
        override val labelRes: Int = R.string.priority_this_week
    }

    data object Someday : Priority {
        override val rank: Int = 4
        override val labelRes: Int = R.string.priority_someday
    }

    companion object {
        val entries: List<Priority> = listOf(Asap, Today, Tomorrow, ThisWeek, Someday)

        fun fromRank(rank: Int): Priority = entries.first { it.rank == rank }
    }
}
