package gg.duel.pinger.data

object ModesList {

  case class Weapon(num: Int)

  val guns = List("fist", "shotgun", "minigun", "rocket launcher", "rifle", "grenade launcher", "pistol", "fireball", "iceball", "slimeball", "bite", "barrel").zipWithIndex.map {
    _.swap
  }.toMap

  object ModeParams extends Enumeration {
    val M_TEAM, M_NOITEMS, M_NOAMMO, M_INSTA, M_EFFICIENCY, M_TACTICS, M_CAPTURE, M_REGEN, M_CTF, M_PROTECT, M_HOLD,
    M_OVERTIME, M_EDIT, M_DEMO, M_LOCAL, M_LOBBY, M_DMSP, M_CLASSICSP, M_SLOWMO, M_COLLECT = Value
  }

  case class Mode(num: Int, name: String, keys: ModeParams.ValueSet)

  val modes = {
    import ModesList.ModeParams._
    val NULL = null
    implicit def toSet(key: ModeParams.Value): ModeParams.ValueSet = ModeParams.ValueSet(key)
    implicit class combine(keySet: ModeParams.ValueSet) {
      def |(key: ModeParams.Value): ModeParams.ValueSet =
        keySet + key
    }
    def mode(name: String, keys: ModeParams.ValueSet, desc: String): Mode = {
      Mode(null.asInstanceOf[Int], name, keys)
    }
    def register(mode: Mode*): Map[Int, Mode] = {
      (for {
        (dada, idx) <- mode.zipWithIndex
        modeNum = idx - 3
      } yield modeNum -> dada.copy(num = modeNum)).toMap
    }

    mode("hey", M_TEAM, "baby")


    register(
      mode("SP", M_LOCAL | M_CLASSICSP, NULL),
      mode("DMSP", M_LOCAL | M_DMSP, NULL),
      mode("demo", M_DEMO | M_LOCAL, NULL),
      mode("ffa", M_LOBBY, "Free For All: Collect items for ammo. Frag everyone to score points."),
      mode("coop edit", M_EDIT, "Cooperative Editing: Edit maps with multiple players simultaneously."),
      mode("teamplay", M_TEAM | M_OVERTIME, "Teamplay: Collect items for ammo. Frag \fs\f3the enemy team\fr to score points for \fs\f1your team\fr."),
      mode("instagib", M_NOITEMS | M_INSTA, "Instagib: You spawn with full rifle ammo and die instantly from one shot. There are no items. Frag everyone to score points."),
      mode("instagib team", M_NOITEMS | M_INSTA | M_TEAM | M_OVERTIME, "Instagib Team: You spawn with full rifle ammo and die instantly from one shot. There are no items. Frag \fs\f3the enemy team\fr to score points for \fs\f1your team\fr."),
      mode("efficiency", M_NOITEMS | M_EFFICIENCY, "Efficiency: You spawn with all weapons and armour. There are no items. Frag everyone to score points."),
      mode("efficiency team", M_NOITEMS | M_EFFICIENCY | M_TEAM | M_OVERTIME, "Efficiency Team: You spawn with all weapons and armour. There are no items. Frag \fs\f3the enemy team\fr to score points for \fs\f1your team\fr."),
      mode("tactics", M_NOITEMS | M_TACTICS, "Tactics: You spawn with two random weapons and armour. There are no items. Frag everyone to score points."),
      mode("tactics team", M_NOITEMS | M_TACTICS | M_TEAM | M_OVERTIME, "Tactics Team: You spawn with two random weapons and armour. There are no items. Frag \fs\f3the enemy team\fr to score points for \fs\f1your team\fr."),
      mode("capture", M_NOAMMO | M_TACTICS | M_CAPTURE | M_TEAM | M_OVERTIME, "Capture: Capture neutral bases or steal \fs\f3enemy bases\fr by standing next to them.  \fs\f1Your team\fr scores points for every 10 seconds it holds a base. You spawn with two random weapons and armour. Collect extra ammo that spawns at \fs\f1your bases\fr. There are no ammo items."),
      mode("regen capture", M_NOITEMS | M_CAPTURE | M_REGEN | M_TEAM | M_OVERTIME, "Regen Capture: Capture neutral bases or steal \fs\f3enemy bases\fr by standing next to them. \fs\f1Your team\fr scores points for every 10 seconds it holds a base. Regenerate health and ammo by standing next to \fs\f1your bases\fr. There are no items."),
      mode("ctf", M_CTF | M_TEAM, "Capture The Flag: Capture \fs\f3the enemy flag\fr and bring it back to \fs\f1your flag\fr to score points for \fs\f1your team\fr. Collect items for ammo."),
      mode("insta ctf", M_NOITEMS | M_INSTA | M_CTF | M_TEAM, "Instagib Capture The Flag: Capture \fs\f3the enemy flag\fr and bring it back to \fs\f1your flag\fr to score points for \fs\f1your team\fr. You spawn with full rifle ammo and die instantly from one shot. There are no items."),
      mode("protect", M_CTF | M_PROTECT | M_TEAM, "Protect The Flag: Touch \fs\f3the enemy flag\fr to score points for \fs\f1your team\fr. Pick up \fs\f1your flag\fr to protect it. \fs\f1Your team\fr loses points if a dropped flag resets. Collect items for ammo."),
      mode("insta protect", M_NOITEMS | M_INSTA | M_CTF | M_PROTECT | M_TEAM, "Instagib Protect The Flag: Touch \fs\f3the enemy flag\fr to score points for \fs\f1your team\fr. Pick up \fs\f1your flag\fr to protect it. \fs\f1Your team\fr loses points if a dropped flag resets. You spawn with full rifle ammo and die instantly from one shot. There are no items."),
      mode("hold", M_CTF | M_HOLD | M_TEAM, "Hold The Flag: Hold \fs\f7the flag\fr for 20 seconds to score points for \fs\f1your team\fr. Collect items for ammo."),
      mode("insta hold", M_NOITEMS | M_INSTA | M_CTF | M_HOLD | M_TEAM, "Instagib Hold The Flag: Hold \fs\f7the flag\fr for 20 seconds to score points for \fs\f1your team\fr. You spawn with full rifle ammo and die instantly from one shot. There are no items."),
      mode("efficiency ctf", M_NOITEMS | M_EFFICIENCY | M_CTF | M_TEAM, "Efficiency Capture The Flag: Capture \fs\f3the enemy flag\fr and bring it back to \fs\f1your flag\fr to score points for \fs\f1your team\fr. You spawn with all weapons and armour. There are no items."),
      mode("efficiency protect", M_NOITEMS | M_EFFICIENCY | M_CTF | M_PROTECT | M_TEAM, "Efficiency Protect The Flag: Touch \fs\f3the enemy flag\fr to score points for \fs\f1your team\fr. Pick up \fs\f1your flag\fr to protect it. \fs\f1Your team\fr loses points if a dropped flag resets. You spawn with all weapons and armour. There are no items."),
      mode("efficiency hold", M_NOITEMS | M_EFFICIENCY | M_CTF | M_HOLD | M_TEAM, "Efficiency Hold The Flag: Hold \fs\f7the flag\fr for 20 seconds to score points for \fs\f1your team\fr. You spawn with all weapons and armour. There are no items."),
      mode("collect", M_COLLECT | M_TEAM, "Skull Collector: Frag \fs\f3the enemy team\fr to drop \fs\f3skulls\fr. Collect them and bring them to \fs\f3the enemy base\fr to score points for \fs\f1your team\fr. Collect items for ammo."),
      mode("insta collect", M_NOITEMS | M_INSTA | M_COLLECT | M_TEAM, "Instagib Skull Collector: Frag \fs\f3the enemy team\fr to drop \fs\f3skulls\fr. Collect them and bring them to \fs\f3the enemy base\fr to score points for \fs\f1your team\fr. You spawn with full rifle ammo and die instantly from one shot. There are no items."),
      mode("efficiency collect", M_NOITEMS | M_EFFICIENCY | M_COLLECT | M_TEAM, "Efficiency Skull Collector: Frag \fs\f3the enemy team\fr to drop \fs\f3skulls\fr. Collect them and bring them to \fs\f3the enemy base\fr to score points for \fs\f1your team\fr. You spawn with all weapons and armour. There are no items.")
    )
  }

  val duelModes = modes.filter(m => Set("ffa", "instagib", "efficiency").contains(m._2.name)).keySet

}
