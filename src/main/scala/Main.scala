package com.hon

/** embedded server */
object Server {
  def main(args: Array[String]): Unit = {
    unfiltered.jetty.Server.http(54833).plan(new BookPlan).run()
  }
}
