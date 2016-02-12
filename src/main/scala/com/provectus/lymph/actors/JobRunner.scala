package com.provectus.lymph.actors

import java.util.concurrent.Executors._

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.util.Timeout
import com.provectus.lymph.{Constants, LymphConfig}
import com.provectus.lymph.actors.tools.Messages.CreateContext
import com.provectus.lymph.jobs.{InMemoryJobRepository, Job, JobConfiguration}
import com.provectus.lymph.contexts._

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

private[lymph] class JobRunner extends Actor {

  // Thread context for parallel running jobs
  val executionContext = ExecutionContext.fromExecutorService(newFixedThreadPool(LymphConfig.Settings.threadNumber))

  // Actor which is creates spark contexts
  lazy val contextManager: ActorRef = context.actorOf(Props[ContextManager], name = Constants.Actors.contextManagerName)

  override def receive: Receive = {
    case configuration: JobConfiguration =>
      val originalSender = sender

      // TODO: add disposable context
      // Time of spark context creating is definitely less than one day
      implicit val timeout = Timeout(1.day)

      // Request spark context creating
      val contextFuture = contextManager ? CreateContext(configuration.name)

      contextFuture.flatMap {
        case contextWrapper: ContextWrapper =>
          val job = new Job(configuration, contextWrapper)
          InMemoryJobRepository.add(job)

          println(s"${configuration.name}#${job.id} is running")

          val future: Future[Either[Map[String, Any], String]] = Future {
            job.run()
          }(executionContext)

          future.andThen {
            case Success(result: Either[Map[String, Any], String]) => originalSender ! result
            case Failure(error: Throwable) => originalSender ! Right(error.toString)
          }(ExecutionContext.global)
      }(ExecutionContext.global)

      // TODO: remove job from memory repository

    // TODO: throw Exception
    case _ => println("Error! JobRequestActor received a message which is not JobConfiguration instance")
  }

}