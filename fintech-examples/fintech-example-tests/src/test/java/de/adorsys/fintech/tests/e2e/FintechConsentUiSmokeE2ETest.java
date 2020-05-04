package de.adorsys.fintech.tests.e2e;

import com.jayway.jsonpath.JsonPath;
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest;
import de.adorsys.fintech.tests.e2e.config.ConsentAuthApproachState;
import de.adorsys.fintech.tests.e2e.config.SmokeConfig;
import de.adorsys.fintech.tests.e2e.steps.FintechServer;
import de.adorsys.fintech.tests.e2e.steps.UserInformationResult;
import de.adorsys.fintech.tests.e2e.steps.WebDriverBasedUserInfoFintech;
import de.adorsys.opba.api.security.external.service.RequestSigningService;
import de.adorsys.opba.api.security.internal.config.TppTokenProperties;
import io.github.bonigarcia.seljup.SeleniumExtension;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;

import static de.adorsys.opba.protocol.xs2a.tests.Const.ENABLE_SMOKE_TESTS;
import static de.adorsys.opba.protocol.xs2a.tests.Const.TRUE_BOOL;
import static de.adorsys.opba.protocol.xs2a.tests.e2e.stages.AisStagesCommonUtil.ANTON_BRUECKNER;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ActiveProfiles("test-mocked-fintech")
@EnabledIfEnvironmentVariable(named = ENABLE_SMOKE_TESTS, matches = TRUE_BOOL)
@ExtendWith({SeleniumExtension.class})
@SpringBootTest(classes = {JGivenConfig.class, SmokeConfig.class}, webEnvironment = NONE)
public class FintechConsentUiSmokeE2ETest extends SpringScenarioTest<FintechServer, WebDriverBasedUserInfoFintech<? extends WebDriverBasedUserInfoFintech<?>>, UserInformationResult> {

    @Autowired
    private SmokeConfig smokeConfig;

    @Autowired
    private ConsentAuthApproachState state;

    @MockBean
    private RequestSigningService requestSigningService;

    @MockBean
    private TppTokenProperties tppTokenProperties;

    @BeforeEach
    void memoizeConsentAuthorizationPreference() {
        state.memoize();
    }

    @AfterEach
    void restoreConsentAuthorizationPreference() {
        state.restore();
    }

    @BeforeAll
    static void setupDriverArch() {
        WebDriverManager.firefoxdriver().arch64();
    }

    @Test
    void testRedirectAntonBruecknerWantsToSeeItsAccountsAndTransanctionsFromFintech(FirefoxDriver firefoxDriver) {
        given().enabled_redirect_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_already_login_in_bank_profile(firefoxDriver)
                .and()
                .user_anton_brueckner_provided_to_consent_ui_initial_parameters_to_list_accounts_with_all_accounts_transactions_consent(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_reviews_transaction_consent_and_accepts(firefoxDriver)
                .and()
                .user_in_consent_ui_sees_redirection_info_to_aspsp_and_accepts(firefoxDriver)
                .and()
                .sandbox_anton_brueckner_from_consent_ui_navigates_to_bank_auth_page(firefoxDriver)
                .and()
                .sandbox_anton_brueckner_inputs_username_and_password(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_confirm_login(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .sandbox_anton_brueckner_selects_sca_method(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .sandbox_anton_brueckner_provides_sca_challenge_result(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .sandbox_anton_brueckner_clicks_redirect_back_to_tpp_button(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_sees_account_and_list_transactions(firefoxDriver);

        String  accountResourceId  = JsonPath.parse(afterLoginAndAuthorizeAccountGetAccountId(firefoxDriver, ANTON_BRUECKNER))
                                             .read("$.accounts[0].resourceId");

        String bankId = JsonPath.parse(loginAndGetBankProfile(firefoxDriver, ANTON_BRUECKNER))
                                 .read("$(bankDescriptor[0].uuid)");

        then()
                .fintech_can_read_anton_brueckner_accounts_and_transactions( accountResourceId , bankId);
    }

    @Test
    public void testEmbeddedMaxMustermanWantsItsAccountsAndTransactionsFromFintech(FirefoxDriver firefoxDriver) {
        given().enabled_embedded_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_already_login_in_bank_profile(firefoxDriver)
                .and()
                .user_max_musterman_provided_to_consent_ui_initial_parameters_to_list_transactions_with_all_accounts_consent(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_reviews_transactions_consent_and_accepts(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_provides_pin(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_sees_sca_select_and_selected_type_email2_to_embedded_authorization(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_provides_sca_result_to_embedded_authorization(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_sees_account_and_list_transactions(firefoxDriver);

        String userId = getInfoId(firefoxDriver);
        String bankId = getBankInfoId(firefoxDriver);

        then().fintech_can_read_max_musterman_accounts_and_transactions(userId, bankId);
    }

    @Test
    void testEmbeddedAntonBruecknerWantsToSeeItsAccountsAndTransanctionsFromFintech(FirefoxDriver firefoxDriver) {
        given().enabled_embedded_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_already_login_in_bank_profile(firefoxDriver)
                .and()
                .user_anton_brueckner_provided_to_consent_ui_initial_parameters_to_list_transactions_with_all_accounts_consent(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_reviews_transaction_consent_and_accepts(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_provides_pin(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_provides_sca_result_to_embedded_authorization(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_sees_account_and_list_transactions(firefoxDriver);

        String userId = getInfoId(firefoxDriver);
        String bankId = getBankInfoId(firefoxDriver);

        then()
                .fintech_can_read_anton_brueckner_accounts_and_transactions(userId, bankId);

    }

    @Test
    public void testUserAfterLoginWantsToLogout(FirefoxDriver firefoxDriver) {
        given().fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_opens_fintechui_login_page(firefoxDriver)
                .and()
                .user_login_with_its_credentials(firefoxDriver)
                .and()
                .user_confirm_login(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_looks_for_a_bank_in_the_bank_search_input_place(firefoxDriver)
                .and()
                .user_wait_for_the_result_in_bank_search(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_back_to_bank_search(firefoxDriver)
                .and()
                .user_after_login_wants_to_logout(firefoxDriver)
                .and()
                .user_click_on_logout_button(firefoxDriver);

        then().fintech_navigates_back_to_login_after_user_logs_out();
    }

    @SneakyThrows
    @Test
    public void testRedirectMaxMustermanToSeeItsAccountsAndTransanctionsFromFintech(FirefoxDriver firefoxDriver) {
        given().enabled_redirect_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_already_login_in_bank_profile(firefoxDriver)
                .and()
                .user_max_musterman_provided_to_consent_ui_initial_parameters_to_list_accounts_with_all_accounts_transactions_consent(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_reviews_transaction_consent_and_accepts(firefoxDriver)
                .and()
                .user_in_consent_ui_sees_redirection_info_to_aspsp_and_accepts(firefoxDriver)
                .and()
                .sandbox_max_musterman_from_consent_ui_navigates_to_bank_auth_page(firefoxDriver)
                .and()
                .sandbox_max_musterman_inputs_username_and_password(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_confirm_login(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_sees_sca_select_and_confirm_type_email2_to_redirect_authorization(firefoxDriver)
                .and()
                .sandbox_max_musterman_provides_sca_challenge_result(firefoxDriver)
                .and()
                .user_max_musterman_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_anton_brueckner_in_consent_ui_sees_thank_you_for_consent_and_clicks_to_tpp(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_sees_account_and_list_transactions(firefoxDriver);

        String userId = getInfoId(firefoxDriver);
        String bankId = getBankInfoId(firefoxDriver);

        then().fintech_can_read_max_musterman_accounts_and_transactions(userId, bankId);
    }

    @SneakyThrows
    private String getInfoId(FirefoxDriver firefoxDriver) {

        String url = firefoxDriver.getCurrentUrl();
        URI uri = new URI(url);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @SneakyThrows
    private String getBankInfoId(FirefoxDriver firefoxDriver) {

        String url = firefoxDriver.getCurrentUrl();
        URI uri = new URI(url);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf("/bank/") + 1);
    }


    private String loginAndGetBankProfile(FirefoxDriver firefoxDriver, String username) {
        given().enabled_redirect_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());
        when().user_opens_fintechui_login_page(firefoxDriver)
                .and()
                .user_login_with_its_credentials(firefoxDriver)
                .and()
                .user_confirm_login(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_looks_for_a_bank_in_the_bank_search_input_place(firefoxDriver)
                .and()
                .user_wait_for_the_result_in_bank_search(firefoxDriver);

        UserInformationResult result = then().fintech_get_bank_infos(username);

        return result.getRespContent();
    }

    private String afterLoginAndAuthorizeAccountGetAccountId(FirefoxDriver firefoxDriver, String username) {
        given().enabled_redirect_sandbox_mode(smokeConfig.getAspspProfileServerUri())
                .fintech_points_to_fintechui_login_page(smokeConfig.getFintechServerUri());

        when().user_opens_fintechui_login_page(firefoxDriver)
                .and()
                .user_login_with_its_credentials(firefoxDriver)
                .and()
                .user_confirm_login(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_looks_for_a_bank_in_the_bank_search_input_place(firefoxDriver)
                .and()
                .user_wait_for_the_result_in_bank_search(firefoxDriver)
                .and()
                .user_navigates_to_page(firefoxDriver)
                .and()
                .user_select_account_button(firefoxDriver)
                .and()
                .user_sees_account_and_list_transactions(firefoxDriver);
        UserInformationResult result = then().fintech_get_user_infos(username);

        return result.getRespContent();
    }






}