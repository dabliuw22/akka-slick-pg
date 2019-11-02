package com.leysoft

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}
import slick.lifted.Tag
import slick.model.Table

object SlickApp extends App {
  implicit val system = ActorSystem("slick-pg")
  implicit val materializer = ActorMaterializer()

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("potgresql")
  implicit val session = SlickSession.forConfig(databaseConfig)
  implicit val userGetResult = User.result

  import session.profile.api._
  Slick.source[User](sql"SELECT * FROM users".as[User])
    .log("user")
    .runWith(Sink.foreach { user => system.log.info(s"$user") })


  Source.single(User(name = "username10"))
    .runWith(Slick.sink(user => sqlu"""INSERT INTO users(name) VALUES (${user.name})"""))

  system.registerOnTermination { () => session.close() }
}

case class User(id: Int = 0, name: String)

object User {

  def result = GetResult(result => User(result.nextInt(), result.nextString()))
}
