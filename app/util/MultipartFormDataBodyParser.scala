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

package util

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{BodyParser, MultipartFormData, PlayBodyParsers}
import play.core.parsers.Multipart._

import scala.concurrent.ExecutionContext

@Singleton
class MultipartFormDataBodyParser @Inject() (parser: PlayBodyParsers)(implicit ec: ExecutionContext) {

  def parse: BodyParser[MultipartFormData[ByteString]] = parser.multipartFormData(filePartAsByteArray)

  val default = parser.default

  def filePartAsByteArray: FilePartHandler[ByteString] = {
    case FileInfo(partName, filename, contentType, _) =>
      val sink = Sink.fold[ByteString, ByteString](ByteString.empty)(_ ++ _)
      val accumulator = Accumulator(sink)
      accumulator.map {
        case b: ByteString =>
          FilePart(partName, filename, contentType, b)
      }
  }
}
