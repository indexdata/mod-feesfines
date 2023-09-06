package org.folio.migration;

import static org.folio.test.support.matcher.FeeFineMatchers.hasAllAutomaticFeeFineTypesFor18_3;
import static org.folio.util.PomUtils.getModuleVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.domain.AutomaticFeeFineType;
import org.folio.rest.persist.PostgresClient;
import org.folio.test.support.ApiTests;
import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;

public class FeeFineTypesDefaultReferenceRecordsTest extends ApiTests {
  private static final String MIGRATION_SCRIPT_18_2_TO_18_3 = loadMigrationScript18_2to18_3();


  @Test
  void reminderFeeIsAddedWhenMigratingFrom18_2To18_3() {
    // module was enabled in @BeforeAll with moduleTo=current_version
    // we must downgrade to 18.2.0 first if we want to rerun the migration script (see RMB-937)
    createTenant(getModuleVersion(), "18.2.0");
    // use SQL to delete, API refuses deleting automatic type
    var deleted = get(PostgresClient.getInstance(vertx, TENANT_NAME)
      .delete(FEEFINES_TABLE, AutomaticFeeFineType.REMINDER_FEE.getId()));
    assertThat(deleted.rowCount(), is(1));

    createTenant("18.2.0", "18.3.0");

    feeFinesClient.getAll().then().body(hasAllAutomaticFeeFineTypesFor18_3());
  }

  private static void createTenant(String moduleFromVersion, String moduleToVersion) {
    final var tenantAttributes = getTenantAttributes()
      .withModuleFrom(MODULE_NAME + "-" + moduleFromVersion)
      .withModuleTo(MODULE_NAME + "-" + moduleToVersion);

    CompletableFuture<Void> future = new CompletableFuture<>();
    createTenant(tenantAttributes, future);
    get(future);
  }

  @Test
  void subsequentRunOfMigrationDoesNotCauseIssues() {
    executeMigration_18_2_to_18_3();
    feeFinesClient.getAll().then()
      .body(hasAllAutomaticFeeFineTypesFor18_3());
  }

  private static String loadMigrationScript18_2to18_3() {
    try (final var resourceAsStream = FeeFineTypesDefaultReferenceRecordsTest.class
      .getResourceAsStream("/templates/db_scripts/" +
        "add-reminder-fee.sql")) {

      return new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8)
        .replaceAll("\\$\\{myuniversity}", TENANT_NAME)
        .replaceAll("\\$\\{mymodule}", MODULE_NAME.replace("-", "_"));

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void executeMigration_18_2_to_18_3() {
    final var future = new CompletableFuture<>();

    PostgresClient.getInstance(Vertx.vertx(), TENANT_NAME)
      .execute(MIGRATION_SCRIPT_18_2_TO_18_3, result -> {
        if (result.succeeded()) {
          future.complete(null);
        } else {
          future.completeExceptionally(result.cause());
        }
      });

    get(future);
  }

}
