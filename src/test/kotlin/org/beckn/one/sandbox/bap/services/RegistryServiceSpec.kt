package org.beckn.one.sandbox.bap.services

import arrow.core.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.beckn.one.sandbox.bap.constants.City
import org.beckn.one.sandbox.bap.constants.Country
import org.beckn.one.sandbox.bap.constants.Domain
import org.beckn.one.sandbox.bap.domain.Subscriber
import org.beckn.one.sandbox.bap.dtos.Error
import org.beckn.one.sandbox.bap.dtos.Response
import org.beckn.one.sandbox.bap.dtos.ResponseStatus
import org.beckn.one.sandbox.bap.errors.registry.RegistryLookupError
import org.beckn.one.sandbox.bap.external.registry.RegistryServiceClient
import org.beckn.one.sandbox.bap.external.registry.SubscriberDto
import org.beckn.one.sandbox.bap.external.registry.SubscriberLookupRequest
import org.beckn.one.sandbox.bap.factories.SubscriberDtoFactory
import org.junit.jupiter.api.Assertions.fail
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import retrofit2.mock.Calls
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class RegistryServiceSpec : DescribeSpec() {
  init {
    val registryServiceClient = mock(RegistryServiceClient::class.java)
    val registryService =
      RegistryService(registryServiceClient, Domain.LocalRetail.value, City.Bengaluru.value, Country.India.value)
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    val request = SubscriberLookupRequest(
      type = Subscriber.Type.BG,
      domain = Domain.LocalRetail.value,
      city = City.Bengaluru.value,
      country = Country.India.value
    )

    describe("Lookup") {
      it("should return subscribers returned by registry") {
        `when`(registryServiceClient.lookup(request)).thenReturn(
          Calls.response(listOf(SubscriberDtoFactory.getDefault(clock = clock)))
        )

        val response: Either<RegistryLookupError, List<SubscriberDto>> = registryService.lookupGateways()

        response
          .fold(
            { fail("Lookup failed. Code: $it.code(), Error: ${it.response().error}") },
            { subscribers -> subscribers shouldBe listOf(SubscriberDtoFactory.getDefault(clock = clock)) }
          )
        verify(registryServiceClient).lookup(request)
      }

      it("should return registry error when registry call fails with an IO exception") {
        `when`(registryServiceClient.lookup(request)).thenReturn(
          Calls.failure(IOException("Timeout"))
        )

        val response: Either<RegistryLookupError, List<SubscriberDto>> = registryService.lookupGateways()

        response
          .fold(
            {
              it.code() shouldBe HttpStatus.INTERNAL_SERVER_ERROR
              it.response() shouldBe Response(
                status = ResponseStatus.NACK,
                error = Error("BAP_001", "Registry lookup returned error")
              )
            },
            { fail("Lookup should have timed out but didn't. Response: $it") }
          )
      }

      it("should return registry error when registry call fails with a runtime exception") {
        `when`(registryServiceClient.lookup(request)).thenReturn(
          Calls.failure(RuntimeException("Network error"))
        )

        val response: Either<RegistryLookupError, List<SubscriberDto>> = registryService.lookupGateways()

        response
          .fold(
            {
              it.code() shouldBe HttpStatus.INTERNAL_SERVER_ERROR
              it.response() shouldBe Response(
                status = ResponseStatus.NACK,
                error = Error("BAP_001", "Registry lookup returned error")
              )
            },
            { fail("Lookup should have timed out but didn't. Response: $it") }
          )
      }

      it("should return no subscribers error when registry response is null") {
        `when`(registryServiceClient.lookup(request)).thenReturn(
          Calls.response(null)
        )

        val response: Either<RegistryLookupError, List<SubscriberDto>> = registryService.lookupGateways()

        response
          .fold(
            {
              it.code() shouldBe HttpStatus.INTERNAL_SERVER_ERROR
              it.response() shouldBe Response(
                status = ResponseStatus.NACK,
                error = Error("BAP_002", "Registry lookup returned null")
              )
            },
            { fail("Lookup should have timed out but didn't. Response: $it") }
          )
      }
    }
  }
}