package com.leysoft.orm

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{Sink, Source}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object SlickAppTwo extends App {
  implicit val system = ActorSystem("slick-pg")
  implicit val materializer = ActorMaterializer()

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("potgresql")
  implicit val session = SlickSession.forConfig(databaseConfig)
  system.registerOnTermination { session.close() }
  val tables = Tables(session)
  val rolesRepository = RolesRepository(tables)
  val userRepository = UserRepository(tables)


  rolesRepository.save(Role(3, "DBA_ROLE"))
    .runWith(Sink.foreach { count => system.log.info(s"save: $count") })

  rolesRepository.findAll
    .runWith(Sink.foreach { role => system.log.info(s"findAll: $role") })

  rolesRepository.findById(3)
    .runWith(Sink.foreach { role => system.log.info(s"findById: $role") })

  userRepository.findAll
    .runWith(Sink.foreach { role => system.log.info(s"findAll: $role") })
}

case class UserRepository(tables: Tables)(implicit session: SlickSession) {
  import session.profile.api._
  import tables._

  def findAll = Slick
    .source((users joinLeft roles on (_.roleId === _.id)).result)
    .map { User.from(_) }
}

case class RolesRepository(tables: Tables)(implicit session: SlickSession) {
  import session.profile.api._
  import tables._

  def save(role: Role) = Source
    .single(role.toTable)
    .via(Slick.flow(roles += _))

  def findAll = Slick
    .source(roles.result)
    .map { Role.from(_) }

  def findById(id: Int) = Slick
    .source(roles.filter { _.id === id }.result)
    .map { Role.from(_) }

  def findByName(name: String) = Slick
    .source(roles.filter { _.name === name }.result)
    .map { Role.from(_) }

  def delete(role: Role) = Source
    .single(role)
    .via(Slick.flow(deleteRole => roles.filter { _.id === deleteRole.id }.delete))

  def update(role: Role) = Source
    .single(role)
    .via(Slick.flow(updateRole => roles
      .filter { _.id === updateRole.id }
      .update(updateRole.toTable)))
}

case class User(id: Int = 0, name: String, role: Option[Role] = None) {

  def toTable = role match {
    case Some(value) => (this.id, this.name, value.id)
    case _ => (this.id, this.name, None)
  }
}

object User {

  def from(result: ((Int, String, Option[Int]), Option[(Int, String)])) = result._2 match {
    case Some(value) => User(id = result._1._1, name = result._1._2, role = Option(Role.from(value)))
    case _ => User(id = result._1._1, name = result._1._2)
  }
}

case class Role(id: Int, name: String) {

  def toTable = (this.id, this.name)
}

object Role {

  def from(result: (Int, String)) = Role(result._1, result._2)
}

case class Tables(session: SlickSession) {
  import session.profile.api._

  val roles = TableQuery[RoleTable]
  val users = TableQuery[UserTable]

  type RoleType = (Int, String)
  type UserType = (Int, String, Option[Int])

  class RoleTable(tag: Tag) extends Table[RoleType](tag, "roles") {
    def id = column[Int]("id")
    def name = column[String]("name")
    def * = (id, name)
  }

  class UserTable(tag: Tag) extends Table[UserType](tag, "users") {
    def id = column[Int]("id")
    def name = column[String]("name")
    def roleId = column[Option[Int]]("role_id")
    def * = (id, name, roleId)
    def role = foreignKey("role_id",
      roleId, roles)(_.id, onDelete=ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
  }
}
