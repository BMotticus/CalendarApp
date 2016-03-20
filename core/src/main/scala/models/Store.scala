package models

import java.time.Instant
import java.time.ZoneId

case class Store(
  id: Long,
  name: String,
  accountId: Long,
  address: Address,
  createdDate: Instant,
  timezone: ZoneId
)

case class StoreInfo(
  id: Long,
  timezone: ZoneId
)