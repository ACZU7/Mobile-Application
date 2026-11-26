package com.example.mobileapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.repository.AuthRepository
import com.example.mobileapplication.data.repository.SignInResult
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Signs in as [role] before the activity starts, so a UI test begins on Home
 * instead of the Login screen. It goes through the real [AuthRepository], with
 * the prototype's demo accounts.
 *
 * Use it as the outer rule around the compose rule:
 * ```
 * private val compose = createAndroidComposeRule<MainActivity>()
 * @get:Rule val rules: RuleChain = RuleChain.outerRule(SignedInRule(Role.ADMIN)).around(compose)
 * ```
 */
class SignedInRule(private val role: Role) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            signIn()
            base.evaluate()
        }
    }

    private fun signIn() = runBlocking {
        val repository = AuthRepository.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        repository.signOut()
        when (role) {
            Role.GUEST -> repository.continueAsGuest()
            Role.RESIDENT -> check(repository.signIn("resident@example.com", "Resident-Demo-2026"))
            Role.ADMIN -> check(repository.signIn("admin@example.com", "Admin-Demo-2026"))
        }
    }

    private fun check(result: SignInResult) {
        check(result is SignInResult.Success) {
            "Could not sign in as $role for the test: $result (wait 30 seconds if sign-in is blocked)"
        }
    }
}
