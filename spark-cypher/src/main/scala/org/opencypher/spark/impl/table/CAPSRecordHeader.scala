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
package org.opencypher.spark.impl.table

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.types.{StructField, StructType}
import org.opencypher.okapi.impl.exception.IllegalArgumentException
import org.opencypher.okapi.ir.api.expr.Var
import org.opencypher.okapi.relational.impl.table._
import org.opencypher.spark.impl.convert.CAPSCypherType._

object CAPSRecordHeader {

  def fromSparkStructType(structType: StructType): RecordHeader =
    RecordHeader.from(structType.fields.map { field =>
      OpaqueField(
        Var(field.name)(field.dataType.toCypherType(field.nullable)
          .getOrElse(throw IllegalArgumentException("a supported Spark type", field.dataType))))
    }: _*)

  def asSparkStructType(header: RecordHeader): StructType = {
    val fields = header.slots.map(slot => structField(slot, !header.mandatory(slot)))
    StructType(fields)
  }

  private def structField(slot: RecordSlot, nullable: Boolean): StructField = {
    val name = ColumnName.of(slot.content)
    val dataType = slot.content.cypherType.getSparkType
    StructField(name, dataType, nullable)
  }

  implicit class CAPSRecordHeader(header: RecordHeader) extends Serializable {
    def asSparkSchema: StructType =
      StructType(header.internalHeader.slots.map(_.asStructField))

    def rowEncoder: ExpressionEncoder[Row] =
      RowEncoder(asSparkSchema)
  }

  implicit class CAPSInternalHeader(internalHeader: InternalHeader) {
    def columns = internalHeader.slots.map(computeColumnName).toVector

    def column(slot: RecordSlot) = columns(slot.index)

    private def computeColumnName(slot: RecordSlot): String = ColumnName.of(slot)
  }

  implicit class CAPSRecordSlot(slot: RecordSlot) {
    def asStructField: StructField = {
      val name = ColumnName.of(slot)
      val sparkType = slot.content.cypherType.getSparkType
      StructField(name, sparkType, slot.content.cypherType.isNullable)
    }
  }
}
