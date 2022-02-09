/*
 * Copyright 2022 HM Revenue & Customs
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

import _root_.deniedrefs.model._
import _root_.model._
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import auth.Actions
import play.api.Logger
import play.api.libs.json.{Json}
import play.api.libs.streams.Accumulator
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.nio.file.{Path, Paths}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeniedRefsController @Inject() (
    as:                Actions,
    cc:                ControllerComponents,
    deniedRefsService: DeniedRefsService
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def uploadDeniedRefs(): Action[Path] = Action.async(pathBodyParser()) { implicit request =>
    val pathToCsv: Path = request.body
    for {
      deniedRefs <- deniedRefsService.parseDeniedRefs(pathToCsv)
      _ <- deniedRefsService.upsert(deniedRefs)
      response = model.UploadDeniedRefsResponse(
        _id      = deniedRefs._id,
        inserted = deniedRefs.inserted,
        size     = deniedRefs.refs.size
      )
    } yield Ok(Json.toJson(response))
  }

  /**
   * This request parser stores incoming request body in file and returns a Path of that file.
   */
  private def pathBodyParser(): BodyParser[Path] = BodyParser[Path] { _: RequestHeader =>
    val path: Path = Paths.get(s"/tmp/${UUID.randomUUID().toString}")
    val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
    Accumulator(sink)
      .map((_: IOResult) => Right(path))
  }

  def verifyRefs(): Action[VerifyRefsRequest] = Action.async(parse.json[VerifyRefsRequest]) { implicit request =>

    val refs: Set[Reference] = request.body.refs

    for {
      _ <- deniedRefsService.updateCacheIfNeeded()
      verifyRefStatus = deniedRefsService.verifyRefs(refs)
    } yield Ok(Json.toJson(VerifyRefResponse(verifyRefStatus)))
  }

  def dropDb(): Action[AnyContent] = Action.async { implicit request =>
    for {
      result <- deniedRefsService.dropDb()
    } yield Ok(Json.obj("denied-refs-collection-dropped" -> result))
  }

  private lazy val logger: Logger = Logger(this.getClass)
}
