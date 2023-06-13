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

package support

/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.{AbstractModule, Provides}
import journeysupport.TestJourneyIdGenerator
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.Result
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import repository.JourneyRepo
import tps.model.JourneyIdGenerator
import javax.inject.Singleton

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */

trait ItSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with WireMockSupport
  with GuiceOneServerPerSuite { self =>

  val testServerPort = 19001

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(5, Seconds)),
    interval = scaled(Span(300, Millis)))

  private val module: AbstractModule = new AbstractModule {
    override def configure(): Unit = ()

    /**
     * This one is randomised every time new test application is spawned. Thanks to that there will be no
     * collisions in database when 2 tests insert journey.
     */
    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def journeyIdGenerator(testJourneyIdGenerator: TestJourneyIdGenerator): JourneyIdGenerator = testJourneyIdGenerator

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def testJourneyIdGenerator(): TestJourneyIdGenerator = new TestJourneyIdGenerator()
  }

  private val configMap: Map[String, Any] = Map[String, Any](
    "mongodb.uri " -> "mongodb://localhost:27017/tps-payments-backend-it",
    "microservice.services.auth.port" -> WireMockSupport.port,
    "microservice.services.tps-payments-backend.protocol" -> "http",
    "microservice.services.tps-payments-backend.host" -> "localhost",
    "microservice.services.tps-payments-backend.port" -> testServerPort
  )

  lazy val injector: Injector = fakeApplication().injector
  lazy val repo: JourneyRepo = injector.instanceOf[JourneyRepo]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(module)))
    .configure(configMap).build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    repo.removeAll().futureValue
    ()
  }

  def status(of: Result): Int = of.header.status

  override implicit protected lazy val runningServer: RunningServer =
    TestServerFactory.start(app)

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port    = Some(testServerPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }
}

