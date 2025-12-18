/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package deniedrefs

import org.apache.pekko.stream.IOResult
import org.apache.pekko.stream.scaladsl.{FileIO, Sink}
import org.apache.pekko.util.ByteString
import deniedrefs.model.UploadDeniedRefsResponse
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc._
import tps.deniedrefs.model.{VerifyRefsRequest, VerifyRefsResponse}
import tps.model.Reference
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.nio.file.{Path, Paths}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeniedRefsController @Inject() (
  cc:                ControllerComponents,
  deniedRefsService: DeniedRefsService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  val uploadDeniedRefs: Action[Path] = Action.async(pathBodyParser()) { implicit request =>
    val pathToCsv: Path = request.body
    for
      deniedRefs <- deniedRefsService.parseDeniedRefs(pathToCsv)
      _          <- deniedRefsService.upsert(deniedRefs)
      response    = UploadDeniedRefsResponse(
                      _id = deniedRefs._id,
                      inserted = deniedRefs.inserted,
                      size = deniedRefs.refs.size
                    )
    yield Ok(Json.toJson(response))
  }

  /** This request parser stores incoming request body in file and returns a Path of that file.
    */
  private def pathBodyParser(): BodyParser[Path] = BodyParser[Path] { (_: RequestHeader) =>
    val path: Path                               = Paths.get(s"/tmp/${UUID.randomUUID().toString}")
    val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
    Accumulator(sink)
      .map((_: IOResult) => Right(path))
  }

  val verifyRefs: Action[VerifyRefsRequest] = Action.async(parse.json[VerifyRefsRequest]) { implicit request =>
    val refs: Set[Reference] = request.body.refs

    for
      _              <- deniedRefsService.updateCacheIfNeeded()
      verifyRefStatus = deniedRefsService.verifyRefs(refs)
    yield Ok(Json.toJson(VerifyRefsResponse(verifyRefStatus)))
  }

}
