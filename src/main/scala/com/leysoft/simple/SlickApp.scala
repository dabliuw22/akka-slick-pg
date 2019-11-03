package com.leysoft.simple

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
  val userRepository = UserRepository()

  userRepository.findAll
    .runWith(Sink.foreach { user => system.log.info(s"findAll: $user") })

  userRepository.save(User(name = "username23"))
    .runWith(Sink.foreach { count => system.log.info(s"save: $count") })

  userRepository.findByName("username23")
    .runWith(Sink.foreach { user => system.log.info(s"findByName: $user") })

  system.registerOnTermination { () => session.close() }
}

case class UserRepository()(implicit val session: SlickSession){
  import session.profile.api._
  private implicit val userResult = User.result
  private implicit val roleResult = Role.result

  def findAll = Slick.source(
    sql"""
          SELECT users.id, users.name, roles.id, roles.name
          FROM users LEFT JOIN roles
          ON users.role_id = roles.id
      """.as[User])

  def findById(id: Int) = Slick.source(
    sql"""
          SELECT users.id, users.name, roles.id, roles.name
          FROM users LEFT JOIN roles
          ON users.role_id = roles.id
          WHERE users.id = '#$id'
       """.as[User])

  def findByName(name: String) = Slick.source(
    sql"""
          SELECT users.id, users.name, roles.id, roles.name
          FROM users LEFT JOIN roles
          ON users.role_id = roles.id
          WHERE users.name = '#$name'
       """.as[User])

  def save(user: User) = Slick
    .source(
      sql"""
            SELECT *
            FROM roles
            WHERE name = '#${Role.`USER_ROLE`}'
        """.as[Role])
    .map { role => User(name = user.name, role = Option(role)) }
    .via(Slick.flow(newUser => newUser.role match {
      case Some(value) =>
        sqlu"""
               INSERT INTO users(name, role_id)
               VALUES ('#${newUser.name}', '#${value.id}')
          """
      case _ =>
        sqlu"""
               INSERT INTO users(name)
               VALUES ('#${newUser.name}')
          """
    }))

  def delete(user: User) = Source.single(user)
    .via(Slick.flow(deleteUser => sqlu"""DELETE FROM users WHERE users.id = #${deleteUser.id}"""))
}

case class User(id: Int = 0, name: String, role: Option[Role] = None)

object User {

  def result = GetResult { result =>
    User(result.nextInt(), result.nextString(), result.nextIntOption()
      .map { Role(_, result.nextString()) })
  }
}

case class Role(id: Int, name: String)

object Role {

  val `USER_ROLE` = "USER_ROLE"

  def result = GetResult(result => Role(result.nextInt(), result.nextString()))
}
