package org.openshouter.domain

object AppSpeakList {
    fun matches(label: String, packageName: String, query: String): Boolean {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return true
        return label.lowercase().contains(needle) || packageName.lowercase().contains(needle)
    }

    fun isSelected(packageName: String, rules: Map<String, AppSpeakRule>): Boolean =
        rules[packageName]?.active == true

    fun include(
        label: String,
        packageName: String,
        query: String,
        selectedOnly: Boolean,
        rules: Map<String, AppSpeakRule>,
    ): Boolean {
        if (!matches(label, packageName, query)) return false
        if (!selectedOnly) return true
        return isSelected(packageName, rules)
    }

    fun allSelected(packageNames: List<String>, rules: Map<String, AppSpeakRule>): Boolean =
        packageNames.isNotEmpty() && packageNames.all { isSelected(it, rules) }
}
