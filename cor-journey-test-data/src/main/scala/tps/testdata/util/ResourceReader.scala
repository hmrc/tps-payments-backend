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

package tps.testdata.util

/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

import scala.io.Source
import scala.util.Using

object ResourceReader {

  def read(resourcePath: String): String = {
    val inputStream = Option(this.getClass.getResourceAsStream(resourcePath))
      .getOrElse(throw new IllegalArgumentException(s"Resource not found: $resourcePath"))

    Using(Source.fromInputStream(inputStream)) { source =>
      source.getLines().mkString("\n")
    }.getOrElse(throw new RuntimeException(s"Failed to read resource: $resourcePath"))
  }
}
