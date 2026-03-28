package com.hulampay.mobile.utils

/**
 * Password strength scoring — mirrors the website's Register/ResetPassword logic.
 *
 * Score breakdown (0–5):
 *   +1  length ≥ 8   (backend minimum)
 *   +1  length ≥ 10
 *   +1  contains uppercase letter
 *   +1  contains digit
 *   +1  contains special char  !@#$%^&*()_+-=
 *
 * Levels:
 *   Weak   score ≤ 1
 *   Medium score ≤ 3
 *   Strong score > 3
 */
fun calculatePasswordStrength(password: String): Int {
    var score = 0
    if (password.length >= 8)  score++
    if (password.length >= 10) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it in "!@#\$%^&*()_+-=" }) score++
    return score
}

enum class PasswordStrengthLevel(val label: String) {
    WEAK("Weak"),
    MEDIUM("Medium"),
    STRONG("Strong"),
}

fun getStrengthLevel(score: Int): PasswordStrengthLevel = when {
    score <= 1 -> PasswordStrengthLevel.WEAK
    score <= 3 -> PasswordStrengthLevel.MEDIUM
    else       -> PasswordStrengthLevel.STRONG
}

/** Returns true if password meets the backend pattern:
 *  ^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=]).{8,}$ */
fun isPasswordValid(password: String): Boolean {
    return password.length >= 8 &&
           password.any { it.isUpperCase() } &&
           password.any { it.isDigit() } &&
           password.any { it in "!@#\$%^&*()_+-=" }
}
