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
import model.Utr.Utr
import org.apache.commons.csv.{CSVFormat, CSVParser, CSVRecord}
import play.api.Logger

import scala.collection.JavaConverters._

class CsvParser {
  def parse(s: String): Seq[Utr] = {

    val lines = CSVParser.parse(s, CSVFormat.DEFAULT).getRecords.asScala
    val utrs = lines.filter(isLineValid).map(cols => Utr(cols.get(0).trim))

    if (utrs.length == lines.length) utrs
    else throw new RuntimeException("File parsing Failed")

  }
  private def isLineValid(line: CSVRecord) = {
    line.size() == 1
  }
  val logger: Logger = Logger(this.getClass)
}
