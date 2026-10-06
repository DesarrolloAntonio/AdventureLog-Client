package com.desarrollodroide.adventurelog.feature.settings

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.domain.repository.AccountError
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.testing.AccountRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.CountriesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.FakeUserRepository
import com.desarrollodroide.adventurelog.core.testing.SettingsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testUser
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupExporter
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupResult
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The edit-profile dialog's save, through the ViewModel that decides when the dialog may close. */
class EditIdentityTest {

    private val dispatcher = StandardTestDispatcher()

    private class Account(private val answer: Either<AccountError, UserDetails>) : AccountRepositoryStub() {
        var saves = 0
        override suspend fun updateProfile(
            username: String?, firstName: String?, lastName: String?, publicProfile: Boolean?,
            measurementSystem: String?, defaultCurrency: String?, mapStyle: String?
        ): Either<AccountError, UserDetails> {
            // A ViewModel that re-sends an accepted change never stops on its own; fail instead.
            if (++saves > 3) throw AssertionError("the same change was sent $saves times")
            return answer
        }
        override suspend fun getMediaUsage(): Either<AccountError, MediaUsage> = Either.Left(AccountError("offline", false))
        override suspend fun getEmailAddresses(): Either<AccountError, List<EmailAddress>> = Either.Left(AccountError("offline", false))
    }

    private val settings = object : SettingsRepositoryStub() {
        override fun getThemeMode(): StateFlow<ThemeMode> = MutableStateFlow(ThemeMode.AUTO)
        override fun getUseDynamicColors(): StateFlow<Boolean> = MutableStateFlow(false)
    }

    private fun viewModel(answer: Either<AccountError, UserDetails>, account: Account = Account(answer)) = SettingsViewModel(
        settingsRepository = settings,
        userRepository = FakeUserRepository(),
        accountRepository = account,
        refreshVisitedRegionsUseCase = RefreshVisitedRegionsUseCase(object : CountriesRepositoryStub() {}),
        backupExporter = object : BackupExporter {
            override suspend fun export(serverUrl: String, fileName: String) = BackupResult.Handed
        }
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a username the server refuses keeps the dialog open with the reason`() = runTest(dispatcher) {
        val vm = viewModel(Either.Left(AccountError("Enter a valid username.", serverRefused = true)))
        testScheduler.advanceUntilIdle()
        val outcomes = mutableListOf<String?>()

        vm.saveIdentity("qa bad!", "John", "Doe") { outcomes += it }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<String?>("Enter a valid username."), outcomes)
    }

    @Test
    fun `a username the server accepts closes the dialog`() = runTest(dispatcher) {
        val vm = viewModel(Either.Right(testUser.copy(username = "qa_new")))
        testScheduler.advanceUntilIdle()
        val outcomes = mutableListOf<String?>()

        vm.saveIdentity("qa_new", "John", "Doe") { outcomes += it }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<String?>(null), outcomes)
    }

    @Test
    fun `an accepted change is sent once`() = runTest(dispatcher) {
        // FakeUserRepository never republishes the session, as a slow echo would not in time.
        val account = Account(Either.Right(testUser.copy(firstName = "Johnny")))
        val vm = viewModel(Either.Right(testUser), account)
        testScheduler.advanceUntilIdle()

        vm.saveIdentity("claude", "Johnny", "")
        testScheduler.advanceUntilIdle()

        assertEquals(1, account.saves)
    }
}
