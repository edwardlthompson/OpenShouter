package org.openshouter.domain

object TtsFormat {
    const val DEFAULT = "%app: %title - %text"
    private val TOKEN = Regex("%(app|title|text|name|number)")

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

    fun notification(template: String, app: String, title: String, text: String): String =
        render(template, mapOf("app" to app, "title" to title, "text" to text))

    fun incomingCall(nameOrNumber: String): String =
        "Incoming call from $nameOrNumber"
}
