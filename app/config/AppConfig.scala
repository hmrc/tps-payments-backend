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

package config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (
    config:         Configuration,
    servicesConfig: ServicesConfig,
    environment:    Environment) {
  val authBaseUrl: String = servicesConfig.baseUrl("auth")
  val emailServiceUrl: String = servicesConfig.baseUrl("email") + "/hmrc/email"
  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  val strideRoles: Set[String] = {
    val roles = config.get[Seq[String]]("stride.roles").toSet
    require(roles.nonEmpty, "Invalid configuration for 'stride.roles' - empty list")
    roles
  }

  val runModeEnvironment: Environment = environment
  val runTimeConfig: Configuration = config
}
