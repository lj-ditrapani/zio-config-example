package info.ditrapani

import zio.ZIO
import zio.console.{putStrLn, getStrLn, Console}

object MyApp extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Console, Throwable, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
    } yield ()
}
