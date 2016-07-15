package models

import java.time.Instant

case class Account(
  id: AccountId,
  name: String,
  phone: Option[String],
  address: Address,
  createdDate: Instant,
  settings: String,
  location: Option[String]
)

case class AccountInfo(
  id: AccountId,
  name: String
)