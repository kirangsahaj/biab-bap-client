package org.beckn.one.sandbox.bap.message.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Clock
import java.time.LocalDateTime

data class Context(
  val domain: Domain,
  val country: String,
  val city: String,
  val action: Action,
  val coreVersion: String,
  val bapId: String,
  val bapUri: String,
  val bppId: String? = null,
  val bppUri: String? = null,
  val transactionId: String,
  val messageId: String,
  @JsonIgnore val clock: Clock = Clock.systemUTC(),
  val timestamp: java.time.LocalDateTime = LocalDateTime.now(clock),
  val key: String? = null,
  val ttl: Duration? = null
) {
  enum class Action(val value: String) {
    SEARCH("search"),
    SELECT("select"),
    INIT("init"),
    CONFIRM("confirm"),
    UPDATE("update"),
    STATUS("status"),
    TRACK("track"),
    CANCEL("cancel"),
    FEEDBACK("feedback"),
    SUPPORT("support"),
    ON_SEARCH("on_search"),
    ON_SELECT("on_select"),
    ON_INIT("on_init"),
    ON_CONFIRM("on_confirm"),
    ON_UPDATE("on_update"),
    ON_STATUS("on_status"),
    ON_TRACK("on_track"),
    ON_CANCEL("on_cancel"),
    ON_FEEDBACK("on_feedback"),
    ON_SUPPORT("on_support"),
    ACK("ack"),
  }
}

typealias Domain = String
typealias Duration = String