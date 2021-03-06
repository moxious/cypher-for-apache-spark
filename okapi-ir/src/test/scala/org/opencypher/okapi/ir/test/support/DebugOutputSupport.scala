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
package org.opencypher.okapi.ir.test.support

import java.util.Objects

import scala.collection.immutable.TreeMap

object Bag {

  // Name for the ordered list representation that we use to check equality of results where the order does not matter
  type Bag[T <: Any] = List[T]

  def apply[E](elements: E*): Bag[E] = {
    // Canonical ordered representation to determine the equality of bags (and have nice diffs when they're not equal)
    implicit val ordering = new Ordering[E] {
      override def compare(x: E, y: E): Int = {
        if (Objects.equals(x, y)) {
          0
        } else if (x == null) {
          -1
        } else if (y == null) {
          1
        } else {
          Ordering[String].compare(x.toString + x.hashCode, y.toString + y.hashCode)
        }
      }
    }
    List(elements: _*).sorted
  }

  implicit class TraversableToBag[E](val t: Traversable[E]) {
    def toBag: Bag[E] = Bag(t.toSeq: _*)
  }

  implicit class IteratorToBag[E](val i: Iterator[E]) {
    def toBag: Bag[E] = Bag(i.toSeq: _*)
  }

  implicit class ArrayToBag[E](val a: Array[E]) {
    def toBag: Bag[E] = Bag(a: _*)
  }

}
