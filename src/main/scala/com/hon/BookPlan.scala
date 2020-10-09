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
import com.google.cloud.datastore.Cursor

case class Book(
    title: String,
    url: String,
    library: List[String],
    isSuggest: Boolean,
    isWakatiGaki: Boolean
)

class BookPlan extends Plan {
  def intent = {
    case r @ GET(Path("/books")) =>
      val datastore = DatastoreOptions.getDefaultInstance().getService()
      val nextPage = r.headers("next").mkString
      val allGetQueryBuilder = Query.newEntityQueryBuilder().setKind("book")
      if (nextPage.isEmpty()) {
        allGetQueryBuilder.setOffset(0).setLimit(10)
      } else allGetQueryBuilder.setStartCursor(Cursor.fromUrlSafe(nextPage))

      val results =
        datastore.run(allGetQueryBuilder.build, Seq.empty[ReadOption]: _*)
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
      implicit val bookEncoder: Encoder[Book] = deriveEncoder
      implicit val booksEncoder: Encoder[List[Book]] = Encoder.encodeList[Book]
      (Ok ~> ContentType("application/json") ~> ResponseHeader(
        "next",
        Seq(results.getCursorAfter().toUrlSafe())
      ) ~> ResponseString(
        books.asJson.noSpaces
      ))
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
          .newBuilder(keyFactory.newKey(book.title))
          .set("title", book.title)
          .set("url", book.url)
          .set("isSuggest", book.isSuggest)
          .set("isWakachiGaki", book.isWakatiGaki)
          .set("library", book.library.mkString(","))
          .build()
      }
      books map (toEntity) foreach { b =>
        datastore.put(b)
      }
      (Ok ~> ContentType("application/json") ~> ResponseString(b))
  }
}
