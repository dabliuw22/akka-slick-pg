package com.leysoft

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

object SlickApp extends App {
  implicit val system = ActorSystem("slick-pg")
  implicit val materializer = ActorMaterializer()

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("potgresql")
  implicit val session = SlickSession.forConfig(databaseConfig)

  import session.profile.api._
  Slick.source[User](
    sql"""
         SELECT users.id, users.name, roles.id, roles.name
         FROM users INNER JOIN roles
         ON users.role_id = roles.id
      """.as[User](User.result))
    .runWith(Sink.foreach { user => system.log.info(s"$user") })

  /*Source.single(User(name = "username10"))
    .runWith(Slick.sink(user => sqlu"INSERT INTO users(name) VALUES (${user.name})"))*/

  system.registerOnTermination { () => session.close() }
}

case class User(id: Int = 0, name: String, role: Option[Role] = None)

object User {

  def result = GetResult { result =>
    User(result.nextInt(), result.nextString(), Some(Role(result.nextInt(), result.nextString())))
  }
}

case class Role(id: Int, name: String)

object Role {

  def result = GetResult(result => Role(result.nextInt(), result.nextString()))
}
