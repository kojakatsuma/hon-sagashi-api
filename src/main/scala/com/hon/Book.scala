package com.hon

import unfiltered.filter._
import unfiltered.request._
import unfiltered.response._
import com.google.cloud.datastore.DatastoreOptions

class Book extends Plan {
  def intent = {
    case r @ GET(Path("/books")) =>
      val body = Body.bytes(r)
      val b = new String(body, "utf-8")
      println(b)
      (Ok ~> ContentType("application/json") ~> ResponseString(b))
    case r @ POST(Path("/books")) =>
      val body = Body.bytes((r))
      val b = new String(body, "utf-8")
      val datastore = DatastoreOptions.getDefaultInstance().getService()
      val key = datastore.newKeyFactory().newKey()
      println(key)
      (Ok ~> ContentType("application/json") ~> ResponseString(b))
  }
}
