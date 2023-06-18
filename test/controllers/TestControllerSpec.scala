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

package controllers

import play.api.test.FakeRequest
import testonly.TestController
import testsupport.ItSpec

class TestControllerSpec extends ItSpec {
  private val controller = injector.instanceOf[TestController]

  "removeTestData" in {
    val result = controller.removeTestData()(FakeRequest()).futureValue
    status(result) shouldBe 200
  }

  "findByReference" in {
    val result = controller.findByReference("someref")(FakeRequest()).futureValue
    status(result) shouldBe 200
  }

}
