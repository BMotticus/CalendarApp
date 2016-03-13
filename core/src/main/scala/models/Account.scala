package models

import java.time.Instant

case class Account(
  id: Long,
  name: String,
  phone: String,
  fax: String,
  address: Address,
  createdDate: Instant,
  settings: String,
  administratorId: Option[Long],
  location: Option[String]
)
