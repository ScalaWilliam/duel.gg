package us.woop.pinger.referencedata

/** 01/02/14 */
object StubServer extends App {


  lazy val mappings: PartialFunction[Vector[Int], Vector[Vector[Int]]] = {
    // server
    case 1 +: 1 +: 1 +: _ =>
      Vector(Vector(5, 5, -128, 3, 1, 3, -128, -61, 1, 17, 1, 102, 114, 111, 122, 101, 110, 0, 115, 97, 117, 101, 114, 46, 119, 111, 111, 112, 46, 117, 115, 0))
    // uptime
    case 0 +: 0 +: -1 +: _ =>
      Vector(Vector(-1, 105, -127, -31, -121, 3, 0, -2, 1, -1, 68, 101, 99, 32, 49, 53, 32, 50, 48, 49, 51, 32, 49, 56, 58, 51, 53, 58, 51, 55, 0))
    // player stats
    case 0 +: 1 +: -1 +: _ =>
      Vector(
        Vector(-1, 105, 0, -11, 0, 33, 119, 48, 48, 112, 124, 68, 114, 46, 65, 107, 107, -121, 0, 103, 111, 111, 100, 0, 0, 0, 0, 0, 0, 100, 0, 6, 3, 5, 91, 121, -74),
        Vector(-1, 105, 0, -11, 1, 42, 107, 105, 110, 103, 0, 103, 111, 111, 100, 0, 11, 0, 11, 0, 22, 1, 0, 4, 0, 0, 5, 15, -101),
        Vector(-1, 105, 0, -11, 4, 49, 85, 101, 122, 0, 103, 111, 111, 100, 0, 9, 0, 14, 0, 20, -99, 0, 4, 0, 1, 109, 29, -77),
        Vector(-1, 105, 0, -11, 5, 30, 71, 105, 102, 116, 122, 90, 0, 103, 111, 111, 100, 0, 14, 0, 11, 0, 28, 1, 0, 4, 0, 0, 109, -64, -8),
        Vector(-1, 105, 0, -11, 2, 84, 78, 101, 120, 117, 115, 0, 103, 111, 111, 100, 0, 18, 0, 16, 0, 33, 1, 0, 4, 0, 0, 46, 78, 86)
      )
    // team stats
    case 0 +: 2 +: _ =>
      Vector(Vector(-1, 105, 1, 3, -128, -61, 1), Vector(9, 14))
  }


}
