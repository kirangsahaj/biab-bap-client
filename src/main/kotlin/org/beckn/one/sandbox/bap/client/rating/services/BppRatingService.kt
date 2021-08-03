package org.beckn.one.sandbox.bap.client.shared.services

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.fasterxml.jackson.databind.ObjectMapper
import org.beckn.one.sandbox.bap.client.external.provider.BppClientFactory
import org.beckn.one.sandbox.bap.client.shared.errors.bpp.BppError
import org.beckn.protocol.schemas.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import retrofit2.Response

@Service
class BppRatingService @Autowired constructor(
  private val bppServiceClientFactory: BppClientFactory,
  private val objectMapper: ObjectMapper
) {
  private val log: Logger = LoggerFactory.getLogger(BppRatingService::class.java)

  private fun isInternalServerError(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.code() == HttpStatus.INTERNAL_SERVER_ERROR.value()

  private fun isBodyNull(httpResponse: Response<ProtocolAckResponse>) = httpResponse.body() == null

  private fun isAckNegative(httpResponse: Response<ProtocolAckResponse>) =
    httpResponse.body()!!.message.ack.status == ResponseStatus.NACK

  fun provideRating(bppUri: String, context: ProtocolContext, refId: String, value: Int):
      Either<BppError, ProtocolAckResponse> =
    Either.catch {
      log.info("Invoking provide rating API on BPP: {}", bppUri)
      val bppServiceClient = bppServiceClientFactory.getClient(bppUri)
      val httpResponse =
        bppServiceClient.provideRating(
          ProtocolRatingRequest(
            context = context,
            message = ProtocolRatingRequestMessage(id = refId, value = value),
          )
        ).execute()
      log.info("BPP provide rating API response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        isInternalServerError(httpResponse) -> Left(BppError.Internal)
        isBodyNull(httpResponse) -> Left(BppError.NullResponse)
        isAckNegative(httpResponse) -> Left(BppError.Nack)
        else -> Right(httpResponse.body()!!)
      }
    }.mapLeft {
      log.error("Error when invoking BPP provide rating API", it)
      BppError.Internal
    }
}