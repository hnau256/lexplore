package hnau.lexplore.projector.common

interface Localizer {

    interface Common {
        val ok: String
        val cancel: String
        val yes: String
        val no: String
        val unknown: String

        interface Duration {
            val shortDays: String
            val shortHours: String
            val shortMinutes: String
            val shortSeconds: String
        }

        val duration: Duration

        interface Months {
            val january: String
            val february: String
            val march: String
            val april: String
            val may: String
            val june: String
            val july: String
            val august: String
            val september: String
            val october: String
            val november: String
            val december: String
        }

        val shortMonths: Months
    }

    val common: Common

    interface DateTimeEdit {
        val day: String
        val month: String
        val year: String
        val hours: String
        val minutes: String
        val seconds: String

        fun unableParseDateTime(text: String): String
    }

    val dateTimeEdit: DateTimeEdit

    interface Logging {
        val address: String
        val port: String
        val portIsIncorrect: String
        val clientId: String
        val randomClientId: String
        val login: String
    }

    val logging: Logging

    interface Logged {

        val logout: String

        val connecting: String

        val waitingForReconnection: String

        val receivingScheme: String

        val failParseSchemeWaitingNewScheme: String

        val schemeParsingError: String
    }

    val logged: Logged

    interface Property {
        val tic: String
        val isEnabled: String
        val value: String
        val edit: String
        val loading: String
        val save: String
        val empty: String
        val unableParseValue: String
        val unableParseDateTime: String
        val timestamp: String

        fun unableParseTextToNumber(text: String): String

        fun unableParseTextToRGB(text: String): String
    }

    val property: Property

    interface Element {
        val properties: String
        val children: String
    }

    val element: Element

    interface Settings {

        val settings: String
    }

    val settings: Settings
}
