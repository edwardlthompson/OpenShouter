package org.openshouter.domain

object TtsFormat {
    const val DEFAULT = "%app: %title - %text"
    const val CALL_DEFAULT = "Incoming call from %name"
    const val MISSED_DEFAULT = "Missed call from %name"
    const val MESSAGE_DEFAULT = "Message from %name: %text"
    const val TIME_DEFAULT = "The time is %time"
    private val TOKEN = Regex(
        "%(app|title|text|name|number|ticker|subtext|bigtext|time|info|bigtitle|bigsummary|lines)",
    )

    fun render(template: String, values: Map<String, String>): String {
        val filled = TOKEN.replace(template) { match ->
            values[match.groupValues[1]].orEmpty()
        }
        return filled
            .replace(Regex("\\s{2,}"), " ")
            .replace(Regex("^[:\\-–]\\s*"), "")
            .replace(Regex("\\s+-\\s*$"), "")
            .trim()
    }

    fun notification(
        template: String,
        app: String,
        title: String,
        text: String,
        extras: Map<String, String> = emptyMap(),
    ): String = render(
        template,
        mapOf("app" to app, "title" to title, "text" to text) + extras,
    )

    fun incomingCall(nameOrNumber: String): String =
        call(CALL_DEFAULT, nameOrNumber)

    fun missedCall(nameOrNumber: String): String =
        call(MISSED_DEFAULT, nameOrNumber)

    fun call(template: String, name: String, number: String = ""): String =
        render(template.ifBlank { CALL_DEFAULT }, mapOf("name" to name, "number" to number))

    fun message(template: String, name: String, text: String): String =
        render(template.ifBlank { MESSAGE_DEFAULT }, mapOf("name" to name, "text" to text))

    fun time(template: String, clock: String): String =
        render(template.ifBlank { TIME_DEFAULT }, mapOf("time" to clock))
}
