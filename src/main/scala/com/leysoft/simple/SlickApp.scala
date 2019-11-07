package com.leysoft.simple

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}

import scala.util.Random

object SlickApp extends App {
  implicit val system = ActorSystem("slick-pg")
  implicit val materializer = ActorMaterializer()

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("potgresql")
  implicit val session = SlickSession.forConfig(databaseConfig)
  val userRepository = UserRepository()

  userRepository.findAll
    .runWith(Sink.foreach { user => system.log.info(s"findAll: $user") })

  userRepository.save(User(name = s"username${Random.between(10, 100)}", score = Random.between(0, 100)))
    .runWith(Sink.foreach { count => system.log.info(s"save: $count") })

  userRepository.findByName("username5")
    .runWith(Sink.foreach { user => system.log.info(s"findByName: $user") })

  userRepository.deleteById(5)
    .runWith(Sink.foreach { count => system.log.info(s"delete: $count") })

  system.registerOnTermination { () => session.close() }
}

case class UserRepository()(implicit val session: SlickSession){
  import session.profile.api._
  private implicit val userResult = User.result
  private implicit val roleResult = Role.result

  def findAll = Slick.source(
    sql"""
          SELECT users.id, users.name, users.score, roles.id, roles.name
          FROM users LEFT JOIN roles
          ON users.role_id = roles.id
      """.as[User])

  def findById(id: Int) = Slick.source(
    sql"""
          SELECT users.id, users.name, users.score, roles.id, roles.name
          FROM users LEFT JOIN roles
          ON users.role_id = roles.id
          WHERE users.id = '#$id'
       """.as[User])

  def findByName(name: String) = Slick.source(
    sql"""
          SELECT users.id, users.name, users.score, roles.id, roles.name
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
               INSERT INTO users(name, score, role_id)
               VALUES ('#${newUser.name}', #${newUser.score}, #${value.id})
          """
      case _ =>
        sqlu"""
               INSERT INTO users(name, score)
               VALUES ('#${newUser.name}', '#${newUser.score}')
          """
    }))

  def deleteById(id: Int) = Source.single(id)
    .via(Slick.flow(userId => sqlu"""DELETE FROM users WHERE users.id = #$id"""))
}

case class User(id: Option[Int] = None, name: String, score: Double = 0.0, role: Option[Role] = None)

object User {

  def result = GetResult { result =>
    User(result.nextIntOption(), result.nextString(), result.nextDouble(), result.nextIntOption()
      .map { Role(_, result.nextString()) })
  }
}

case class Role(id: Int, name: String)

object Role {

  val `USER_ROLE` = "USER_ROLE"

  def result = GetResult(result => Role(result.nextInt(), result.nextString()))
}
