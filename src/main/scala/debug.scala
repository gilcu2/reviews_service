import cats.effect._

object debug {
  implicit class DebugHelper[A](ioa: IO[A]) {
    def debug_thread: IO[A] =
      for {
        a <- ioa
        tn = Thread.currentThread.getName
        _ = println(s"[$tn] $a")
      } yield a
  }
}