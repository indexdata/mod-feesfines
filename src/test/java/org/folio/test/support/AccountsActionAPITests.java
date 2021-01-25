package org.folio.test.support;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.response.Response;
import java.util.Date;
import org.folio.rest.jaxrs.model.Account;
import org.folio.rest.utils.ResourceClient;

public abstract class AccountsActionAPITests extends ApiTests {

  public static Account verifyAccountAndGet(
    ResourceClient accountsClient,
    String accountId,
    String expectedPaymentStatus,
    float amount,
    String statusName) {

    final Response getAccountByIdResponse = accountsClient.getById(accountId);
    getAccountByIdResponse
      .then()
      .body("remaining", is(amount))
      .body("status.name", is(statusName))
      .body("paymentStatus.name", is(expectedPaymentStatus))
      .body("dateUpdated", notNullValue())
      .body("metadata.updatedDate", notNullValue());

    final Account updatedAccount = getAccountByIdResponse.as(Account.class);
    final Date dateCreated = updatedAccount.getMetadata().getCreatedDate();
    final Date dateUpdated = updatedAccount.getMetadata().getUpdatedDate();

    assertThat(dateUpdated, equalTo(updatedAccount.getDateUpdated()));
    assertThat(dateCreated, not(equalTo(dateUpdated)));

    return updatedAccount;
  }
}
