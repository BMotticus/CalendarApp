package models

import java.time.Instant
import java.time.ZoneId

class Store(
  id: Long,
  name: String,
  accountId: Long,
  address: Address,
  createdDate: Instant,
  timezone: ZoneId
)
