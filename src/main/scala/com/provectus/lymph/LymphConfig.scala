package com.provectus.lymph

import java.util.concurrent.TimeUnit

import com.typesafe.config.{ConfigException, ConfigFactory, Config}

import collection.JavaConversions._
import scala.concurrent.duration.FiniteDuration

/** Configuration wrapper */
private[lymph] object LymphConfig {

  private val config = ConfigFactory.load()

  /** Common application settings */
  object Settings {
    private val settings = config.getConfig("lymph.settings")

    /** Max number of threads for JVM where jobs are running */
    lazy val threadNumber: Int = settings.getInt("threadNumber")
  }

  /** HTTP specific settings */
  object HTTP {
    private val http = config.getConfig("lymph.http")

    /** To start HTTP server or not to start */
    val isOn: Boolean = http.getBoolean("on")

    /** HTTP server host */
    lazy val host: String = http.getString("host")
    /** HTTP server port */
    lazy val port: Int = http.getInt("port")
  }

  /** Settings for each spark context */
  object Spark {
    private val spark = config.getConfig("lymph.spark")

    /** Spark master server url
      *
      * Any clear for spark string:
      * local[*]
      * spark://host:7077
      * mesos://host:5050
      * yarn
      */
    lazy val master: String = spark.getString("master")
  }

  /** MQTT specific settings */
  object MQTT {
    private val mqtt = config.getConfig("lymph.mqtt")

    /** To start MQTT subscriber on not to start */
    val isOn: Boolean = mqtt.getBoolean("on")

    /** MQTT host */
    lazy val host: String = mqtt.getString("host")
    /** MQTT port */
    lazy val port: Int = mqtt.getInt("port")
    /** MQTT topic used for ''reading'' */
    lazy val subscribeTopic: String = mqtt.getString("subscribeTopic")
    /** MQTT topic used for ''writing'' */
    lazy val publishTopic: String = mqtt.getString("publishTopic")
  }


  /** Settings for all contexts generally and for each context particularly */
  object Contexts {
    private val contexts = config.getConfig("lymph.contexts")
    private val contextDefaults = config.getConfig("lymph.contextDefaults")
    private val contextSettings = config.getConfig("lymph.contextSettings")

    /** Flag of context creating on start or on demand */
    lazy val precreated: List[String] = contextSettings.getStringList("onstart").toList

    /** Return config for specified context or default settings
      *
      * @param contextName    context name
      * @return               config for `contextName` or default config
      */
    private def getContextOrDefault(contextName: String): Config = {
      var contextConfig:Config = null
      try {
        contextConfig = contexts.getConfig(contextName).withFallback(contextDefaults)
      }
      catch {
        case _:ConfigException.Missing =>
          contextConfig = contextDefaults
      }
      contextConfig
    }

    /** Waiting for job completion timeout */
    def timeout(contextName: String): FiniteDuration = {
      FiniteDuration(getContextOrDefault(contextName).getDuration("timeout").toNanos, TimeUnit.NANOSECONDS)
    }

  }
}