package hnau.lexplore.compose.utils

import hnau.lexplore.projector.common.Localizer

data object LocalizerImpl : Localizer {

    override val common = object : Localizer.Common {
        override val ok: String = "Ok"
        override val cancel: String = "Cancel"
        override val yes: String = "Yes"
        override val no: String = "No"
        override val unknown: String = "<unknown>"

        override val duration = object : Localizer.Common.Duration {
            override val shortDays: String = "d"
            override val shortHours: String = "h"
            override val shortMinutes: String = "m"
            override val shortSeconds: String = "s"
        }

        override val shortMonths = object : Localizer.Common.Months {
            override val january: String = "Jan"
            override val february: String = "Feb"
            override val march: String = "Mar"
            override val april: String = "Apr"
            override val may: String = "May"
            override val june: String = "Jun"
            override val july: String = "Jul"
            override val august: String = "Aug"
            override val september: String = "Sep"
            override val october: String = "Oct"
            override val november: String = "Nov"
            override val december: String = "Dec"
        }
    }

    override val dateTimeEdit: Localizer.DateTimeEdit = object : Localizer.DateTimeEdit {
        override val day: String = "Day"
        override val month: String = "Month"
        override val year: String = "Year"
        override val hours: String = "Hours"
        override val minutes: String = "Minutes"
        override val seconds: String = "Seconds"

        override fun unableParseDateTime(text: String): String =
            "Unable parse '$text' to date and time"
    }

    override val logging = object : Localizer.Logging {
        override val login: String = "Login"
        override val address: String = "Address"
        override val port: String = "Port"
        override val portIsIncorrect: String = "Port is incorrect"
        override val clientId: String = "ClientId"
        override val randomClientId: String = "Random  ClientId"
    }
    override val logged = object : Localizer.Logged {

        override val logout: String = "Logout"

        override val connecting: String = "Connecting"

        override val waitingForReconnection: String = "Waiting for reconnection"

        override val receivingScheme: String = "Receiving scheme"

        override val failParseSchemeWaitingNewScheme: String =
            "Fail parse scheme. Waiting new scheme"

        override val schemeParsingError: String = "Scheme parsing error"
    }

    override val element = object : Localizer.Element {
        override val properties: String = "Properties"
        override val children: String = "Children"
    }

    override val property = object : Localizer.Property {
        override val tic: String = "Tic"
        override val isEnabled: String = "Is enabled"
        override val value: String = "Value"
        override val edit: String = "Edit"
        override val loading: String = "Loading"
        override val save: String = "Save"
        override val empty: String = "<empty>"
        override val unableParseValue: String = "Unable parse value"
        override val unableParseDateTime: String = "Unable parse date or time"
        override val timestamp: String = "Timestamp"

        override fun unableParseTextToNumber(text: String): String =
            "Unable parse text '$text' to number"

        override fun unableParseTextToRGB(text: String): String =
            "Unable parse text '$text' to #RGB"
    }

    override val settings = object : Localizer.Settings {
        override val settings: String = "Settings"
    }
}
