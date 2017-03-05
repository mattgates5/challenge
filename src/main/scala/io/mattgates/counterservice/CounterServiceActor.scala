package io.mattgates.counterservice

import akka.actor.Actor
import akka.event.Logging._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}

/**
  * Created by mgates on 10/8/16.
  */
class CounterServiceActor extends Actor with CounterServiceAPI {
  def actorRefFactory = context

  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(res) => Some(LogEntry(req.method + ":" + req.uri + ":" + res.status, InfoLevel))
    case _ => None
  }

  def routeWithLogging = DebuggingDirectives.logRequestResult(requestMethodAndResponseStatusAsInfo _)(route)
  def receive = runRoute(routeWithLogging)
}
