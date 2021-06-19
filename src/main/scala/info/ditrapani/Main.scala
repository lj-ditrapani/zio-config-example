package info.ditrapani

import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import com.typesafe.config.ConfigFactory
import zio.{ExitCode, Has, IO, URIO, ZEnv, ZIO, ZLayer}
import zio.config.typesafe.TypesafeConfigSource
import zio.console.{Console, putStrLn}
import zio.config.{ConfigDescriptor, ConfigSource, ReadError, getConfig, read}

import java.net.URI

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.provideCustomLayer(configLayer).exitCode

  val app: ZIO[Console with Has[Config], Throwable, Unit] =
    for {
      config <- getConfig[Config]
      _ <- putStrLn("config.toString()")
      _ <- putStrLn(config.toString())
    } yield ()

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

final case class Config(database: Database, upstreamApi: UpstreamApi)
final case class Database(host: URI, port: Int, database: String, user: String, password: String)
final case class UpstreamApi(host: URI, port: Int)
