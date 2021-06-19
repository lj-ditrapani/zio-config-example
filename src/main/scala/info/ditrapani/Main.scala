package info.ditrapani

import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import com.typesafe.config.ConfigFactory
import zio.{ExitCode, Has, Layer, URIO, ZEnv, ZIO}
import zio.config.typesafe.TypesafeConfig
import zio.console.{Console, putStrLn}
import zio.config.{ConfigDescriptor, ReadError, getConfig}

import java.net.URI

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.provideCustomLayer(configLayer).exitCode

  val app: ZIO[Console with Has[Config], Throwable, Unit] =
    for {
      config <- getConfig[Config]
      _ <- putStrLn(config.toString())
    } yield ()

  val configLayer: Layer[ReadError[String], Has[Config]] = {
    val configDescriptor: ConfigDescriptor[Config] = descriptor[Config]
    TypesafeConfig.fromTypesafeConfig[Config](
      ConfigFactory.defaultApplication(),
      configDescriptor,
    )
  }
}

final case class Config(database: Database, upstreamApi: UpstreamApi)
final case class Database(host: URI, port: Int, database: String, user: String, password: String)
final case class UpstreamApi(host: URI, port: Int)
