package com.example

import com.example.data.model.DestinationType
import com.example.data.model.ForwardRule
import com.example.data.model.MatchMode
import com.example.service.RuleMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleMatcherTest {

    @Test
    fun `test any sender and any content matches`() {
        val rule = ForwardRule(
            id = 1,
            name = "Match All",
            isEnabled = true,
            senderMatchMode = MatchMode.ANY,
            contentMatchMode = MatchMode.ANY,
            destinationType = DestinationType.WEBHOOK,
            webhookUrl = "https://example.com"
        )
        assertTrue(RuleMatcher.matches(rule, "+14155550199", "Any message text"))
    }

    @Test
    fun `test disabled rule does not match`() {
        val rule = ForwardRule(
            id = 1,
            name = "Disabled",
            isEnabled = false,
            senderMatchMode = MatchMode.ANY,
            contentMatchMode = MatchMode.ANY
        )
        assertFalse(RuleMatcher.matches(rule, "+14155550199", "Any message text"))
    }

    @Test
    fun `test contains match for OTP message`() {
        val rule = ForwardRule(
            id = 2,
            name = "OTP Rule",
            isEnabled = true,
            senderMatchMode = MatchMode.ANY,
            contentMatchMode = MatchMode.CONTAINS,
            contentPattern = "OTP"
        )
        assertTrue(RuleMatcher.matches(rule, "+12345", "Your OTP code is 987123"))
        assertFalse(RuleMatcher.matches(rule, "+12345", "Hello there, how are you?"))
    }

    @Test
    fun `test sender starts with match`() {
        val rule = ForwardRule(
            id = 3,
            name = "Bank Filter",
            isEnabled = true,
            senderMatchMode = MatchMode.STARTS_WITH,
            senderPattern = "+1415",
            contentMatchMode = MatchMode.ANY
        )
        assertTrue(RuleMatcher.matches(rule, "+14155550199", "Hello"))
        assertFalse(RuleMatcher.matches(rule, "+442071234567", "Hello"))
    }

    @Test
    fun `test regex pattern match`() {
        val rule = ForwardRule(
            id = 4,
            name = "Regex Matcher",
            isEnabled = true,
            senderMatchMode = MatchMode.ANY,
            contentMatchMode = MatchMode.REGEX,
            contentPattern = "\\b\\d{6}\\b" // 6-digit code
        )
        assertTrue(RuleMatcher.matches(rule, "SENDER", "Passcode: 654321 is valid"))
        assertFalse(RuleMatcher.matches(rule, "SENDER", "No digits here"))
    }
}
