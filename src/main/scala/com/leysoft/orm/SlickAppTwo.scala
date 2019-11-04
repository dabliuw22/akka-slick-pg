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

  userRepository.save(User(name = "user67", role = Some(Role(Option(1), "USER_ROLE"))))
    .runWith(Sink.foreach { count => system.log.info(s"save: $count") })

  rolesRepository.save(Role(name = "DBA_ROLE"))
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

  def save(user: User) = Source
    .single(user.toTable)
    .via(Slick.flow(users += _))

  def findAll = Slick
    .source((users joinLeft roles on (_.roleId === _.id)).result)
    .map { User.from(_) }

  def findById(id: Int) = Slick
    .source((users joinLeft roles on (_.roleId === _.id))
      .filter { _._1.id === id }.result)

  def findByName(name: String) = Slick
    .source((users joinLeft roles on (_.roleId === _.id))
      .filter { _._1.name === name }.result)
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

case class User(id: Option[Int] = None, name: String, role: Option[Role] = None) {

  def toTable = role match {
    case Some(value) => (this.id, this.name, value.id)
    case _ => (this.id, this.name, None)
  }
}

object User {

  def from(result: ((Option[Int], String, Option[Int]), Option[(Option[Int], String)])) = result._2 match {
    case Some(value) => User(id = result._1._1, name = result._1._2, role = Option(Role.from(value)))
    case _ => User(id = result._1._1, name = result._1._2)
  }
}

case class Role(id: Option[Int] = None, name: String) {

  def toTable = (this.id, this.name)
}

object Role {

  def from(result: (Option[Int], String)) = Role(result._1, result._2)
}

case class Tables(session: SlickSession) {
  import session.profile.api._

  val roles = TableQuery[RoleTable]
  val users = TableQuery[UserTable]

  type RoleType = (Option[Int], String)
  type UserType = (Option[Int], String, Option[Int])

  class RoleTable(tag: Tag) extends Table[RoleType](tag, "roles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name)
  }

  class UserTable(tag: Tag) extends Table[UserType](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def roleId = column[Option[Int]]("role_id")
    def * = (id.?, name, roleId)
    def role = foreignKey("role_id",
      roleId, roles)(_.id, onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Restrict)
  }
}
