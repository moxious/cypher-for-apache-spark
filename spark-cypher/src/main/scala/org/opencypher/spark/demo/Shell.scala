/*
 * Copyright (c) 2016-2018 "Neo4j, Inc." [https://neo4j.com]
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.spark.demo

import ammonite.repl.FrontEnd
import ammonite.util.{Bind, Util}
import org.opencypher.okapi.relational.CAPS

object Shell {

  def main(args: Array[String]): Unit = {
    implicit val session = CSVDemo.sparkSession
    try {
      val welcomeBanner = {
        val ownVersion = CAPS.version.getOrElse("<unknown>")
        val ammoniteVersion = ammonite.Constants.version
        val scalaVersion = scala.util.Properties.versionNumberString
        val javaVersion = System.getProperty("java.version")
        val sparkVersion = session.version
        Util.normalizeNewlines(
          """  _____          __             ___
            = / ___/_ _____  / /  ___ ____  / _/__  ____
            =/ /__/ // / _ \/ _ \/ -_) __/ / _/ _ \/ __/
            =\___/\_, / .__/_//_/\__/_/   /_/ \___/_/
            =   _/___/_/            __         ____              __
            =  / _ | ___  ___ _____/ /  ___   / __/__  ___ _____/ /__
            = / __ |/ _ \/ _ `/ __/ _ \/ -_) _\ \/ _ \/ _ `/ __/  '_/
            =/_/ |_/ .__/\_,_/\__/_//_/\__/ /___/ .__/\_,_/_/ /_/\_\
            =     /_/                          /_/
            =""".stripMargin('=') +
          s"""|
              |Version $ownVersion
              |(Apache Spark $sparkVersion, Scala $scalaVersion, Java $javaVersion, Ammonite $ammoniteVersion)
              |
              |Cypher is a registered trademark of Neo4j, Inc.
              |
           """.stripMargin
        )
      }
      val frontend =
        if (System.getProperty("os.name").startsWith("Windows"))
          FrontEnd.JLineWindows
        else
          FrontEnd.JLineUnix

      new ammonite.Main(
        welcomeBanner = Some(welcomeBanner),
        predefCode =
          s"""|import org.opencypher.caps.demo.CSVDemo._
              |""".stripMargin
      ).instantiateRepl(Vector(Bind("session", session)), None) match {
        case Right(repl) =>
          repl.prompt.bind("(:Cypher)-[:FOR]->(:Apache:Spark) ")
          repl.frontEnd.bind(frontend)
          repl.initializePredef()
          repl.run()

        case Left((failure, paths)) =>
          throw new RuntimeException(s"${failure.msg} [$paths]")
      }
    } finally {
      session.stop()
    }

    // Needed; otherwise the shell hangs on exit
    System.exit(0)
  }
}
