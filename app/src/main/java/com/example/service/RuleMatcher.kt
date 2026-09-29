package com.example.service

import com.example.data.model.ForwardRule
import com.example.data.model.MatchMode

object RuleMatcher {

    fun matches(rule: ForwardRule, sender: String, content: String): Boolean {
        if (!rule.isEnabled) return false

        val senderMatches = checkMatch(
            target = sender,
            pattern = rule.senderPattern,
            mode = rule.senderMatchMode
        )
        if (!senderMatches) return false

        val contentMatches = checkMatch(
            target = content,
            pattern = rule.contentPattern,
            mode = rule.contentMatchMode
        )
        return contentMatches
    }

    private fun checkMatch(target: String, pattern: String, mode: MatchMode): Boolean {
        return when (mode) {
            MatchMode.ANY -> true
            MatchMode.CONTAINS -> {
                if (pattern.isBlank()) true
                else target.contains(pattern, ignoreCase = true)
            }
            MatchMode.EQUALS -> {
                if (pattern.isBlank()) true
                else target.trim().equals(pattern.trim(), ignoreCase = true)
            }
            MatchMode.STARTS_WITH -> {
                if (pattern.isBlank()) true
                else target.trim().startsWith(pattern.trim(), ignoreCase = true)
            }
            MatchMode.REGEX -> {
                if (pattern.isBlank()) true
                else try {
                    Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(target)
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
