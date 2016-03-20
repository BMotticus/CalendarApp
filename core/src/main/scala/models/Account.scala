package models

import java.time.Instant

case class Account(
  id: Long,
  name: String,
  phone: Option[String],
  address: Address,
  createdDate: Instant,
  settings: String,
  location: Option[String]
)

case class AccountInfo(
  id: Long,
  name: String
)