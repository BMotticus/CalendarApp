package models

import java.time.Instant
import java.time.ZoneId

case class Store(
  id: StoreId,
  name: String,
  accountId: AccountId,
  address: Address,
  createdDate: Instant,
  timezone: ZoneId
)

case class StoreInfo(
  id: StoreId,
  timezone: ZoneId
)