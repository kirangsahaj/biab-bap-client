package org.beckn.one.sandbox.bap.message.entities

import org.beckn.one.sandbox.bap.Default

data class QuotationDao @Default constructor(
  val price: PriceDao? = null,
  val breakup: List<QuotationBreakupDao>? = null,
  val ttl: String? = null
)


data class QuotationBreakupDao @Default constructor(
  val type: Type? = null,
  val refId: String? = null,
  val title: String? = null,
  val price: PriceDao? = null
) {

  enum class Type(val value: String) {
    ITEM("item"),
    OFFER("offer"),
    ADDON("add-on"),
    FULFILLMENT("fulfillment");
  }
}