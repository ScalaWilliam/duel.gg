package gg.duel

import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.{Filter, EssentialFilter}
import play.filters.gzip.GzipFilter

import scala.concurrent.ExecutionContext

/**
 * Created on 30/07/2015.
 */

class Filters @Inject()(gzipFilter: GzipFilter)(implicit executionContext: ExecutionContext) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = {
    Seq(Filter{
      case (next, rh) =>
        next(rh).map(_.withHeaders("Access-Control-Allow-Origin" -> "*"))
    }, gzipFilter)
  }
}