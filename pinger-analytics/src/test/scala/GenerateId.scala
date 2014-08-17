
object GenerateId extends App {

  var chars = "aunthonathttp"
  val rand = new scala.util.Random()
  def random: Char = {
    val rnd = rand.nextInt(chars.size)
    chars(rnd)
  }

  (1 to 60) map (_ => Vector.fill(10)(random)) map (_.mkString("")) foreach println

}
