package com.hon

import unfiltered.filter._
import unfiltered.request._
import unfiltered.response._
import com.google.cloud.datastore.DatastoreOptions
import io.circe._, io.circe.parser._, io.circe.generic.semiauto._,
io.circe.syntax._
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.ReadOption

case class Book(
    title: String,
    url: String,
    library: List[String],
    isSuggest: Boolean,
    isWakachiGaki: Boolean
)

class BookPlan extends Plan {
  def intent = {
    case GET(Path("/books")) =>
      val datastore = DatastoreOptions.getDefaultInstance().getService()
      val allGetQuery = Query
        .newGqlQueryBuilder(Query.ResultType.ENTITY, "select * from book")
        .build()
      val results = datastore.run(allGetQuery, Seq.empty[ReadOption]: _*)
      var books = List[Book]()
      while (results.hasNext()) {
        val entity = results.next()
        books = books :+ Book(
          entity.getString("title"),
          entity.getString("url"),
          entity.getString("library").split(",").toList,
          entity.getBoolean("isSuggest"),
          entity.getBoolean("isWakachiGaki")
        )
      }
      implicit val bookEncoder: Encoder[Book] =deriveEncoder
      implicit val booksEncoder: Encoder[List[Book]] = Encoder.encodeList[Book]
      
      (Ok ~> ContentType("application/json") ~> ResponseString(books.asJson.noSpaces))
    case r @ POST(Path("/books")) =>
      val body = Body.bytes((r))
      val b = new String(body, "utf-8")
      val datastore = DatastoreOptions.getDefaultInstance().getService()
      val keyFactory = datastore.newKeyFactory().setKind("book")
      implicit val bookDecoder: Decoder[Book] = deriveDecoder
      val result = decode[List[Book]](b)
      val books = result match {
        case Left(_)      => List[Book]()
        case Right(books) => books
      }
      def toEntity(book: Book) = {
        Entity
          .newBuilder(keyFactory.newKey(book.url))
          .set("title", book.title)
          .set("url", book.url)
          .set("isSuggest", book.isSuggest)
          .set("isWakachiGaki", book.isWakachiGaki)
          .set("library", book.library.mkString(","))
          .build()
      }
      books map (toEntity) foreach { b =>
        datastore.put(b)
      }
      (Ok ~> ContentType("application/json") ~> ResponseString(b))
  }
}
