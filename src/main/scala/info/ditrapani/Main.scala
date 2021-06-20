package info.ditrapani

import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import com.typesafe.config.ConfigFactory
import zio.{ExitCode, Has, IO, RIO, RLayer, UIO, URIO, ZEnv, ZIO, ZLayer}
import zio.config.typesafe.TypesafeConfigSource
import zio.console.Console
import zio.config.{ConfigDescriptor, ConfigSource, ReadError, read}

import java.net.URI

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val (databaseConfigLayer, apiConfigLayer): (
        ZLayer[ZEnv, ReadError[String], Has[DatabaseConfig]],
        ZLayer[ZEnv, ReadError[String], Has[UpstreamApiConfig]],
    ) = {
      import zio.config.syntax._
      (configLayer.narrow(_.database), configLayer.narrow(_.upstreamApi))
    }
    val dbLayer: RLayer[ZEnv, Has[Database]] =
      (ZLayer.identity[Console] ++ databaseConfigLayer) >>> (DatabaseLive.layer)
    val apiLayer: RLayer[ZEnv, Has[UpstreamApi]] =
      (ZLayer.identity[Console] ++ apiConfigLayer) >>> (UpstreamApiLive.layer)
    app.provideCustomLayer(dbLayer ++ apiLayer).exitCode
  }

  val app: RIO[Console with Has[Database] with Has[UpstreamApi], Unit] =
    for {
      dbResult <- Database.query()
      _ <- UpsteramApi.post(dbResult)
    } yield ()

  trait Database {
    def query(): UIO[String]
  }

  final class DatabaseLive(console: Console.Service, config: DatabaseConfig) extends Database {
    override def query(): UIO[String] = for {
      _ <- console.putStrLn(s"query db ${config.host}:${config.port} ${config.database}").orDie
    } yield "dbResult"
  }

  object DatabaseLive {
    val layer: RLayer[Console with Has[DatabaseConfig], Has[Database]] =
      (new DatabaseLive(_, _)).toLayer
  }

  object Database {
    def query(): URIO[Has[Database], String] = ZIO.serviceWith[Database](_.query())
  }

  trait UpstreamApi {
    def post(payload: String): UIO[Unit]
  }

  final class UpstreamApiLive(console: Console.Service, config: UpstreamApiConfig)
      extends UpstreamApi {
    override def post(payload: String): UIO[Unit] =
      console.putStrLn(s"POST $payload to ${config.host}:${config.port}").orDie
  }

  object UpstreamApiLive {
    val layer: RLayer[Console with Has[UpstreamApiConfig], Has[UpstreamApi]] =
      (new UpstreamApiLive(_, _)).toLayer
  }

  object UpsteramApi {
    def post(payload: String): URIO[Has[UpstreamApi], Unit] =
      ZIO.serviceWith[UpstreamApi](_.post(payload))
  }

  val loadConfigDescriptor: ZIO[ZEnv, ReadError[String], ConfigDescriptor[Config]] = {

    val configDescriptor: ConfigDescriptor[Config] = descriptor[Config]
    for {
      envConfigSource <- ConfigSource.fromSystemEnv(
        keyDelimiter = Some('_'),
        valueDelimiter = Some(','),
      )
      typesafeConfigSource <- IO.fromEither(
        TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()),
      )
      sources: ConfigSource = envConfigSource <> typesafeConfigSource
    } yield {
      configDescriptor.updateSource(_ => sources)
    }
  }

  val configLayer: ZLayer[ZEnv, ReadError[String], Has[Config]] = {
    loadConfigDescriptor.map(read[Config](_)).absolve.toLayer
  }
}

final case class Config(database: DatabaseConfig, upstreamApi: UpstreamApiConfig)
final case class DatabaseConfig(
    host: URI,
    port: Int,
    database: String,
    user: String,
    password: String,
)
final case class UpstreamApiConfig(host: URI, port: Int)
