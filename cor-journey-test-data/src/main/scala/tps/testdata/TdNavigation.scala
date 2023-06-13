package tps.testdata

import tps.model.Navigation

trait TdNavigation {

  def navigation: Navigation = Navigation(
    back =  "http://localhost:9124/tps-payments/landing",
    reset =  "http://localhost:9124/tps-payments/full/reset",
    finish =  "http://localhost:9124/tps-payments/finish",
    callback =  "http://localhost:9124/payments/notifications/send-card-payments"
  )

}
