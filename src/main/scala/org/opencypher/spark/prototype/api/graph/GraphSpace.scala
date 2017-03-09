package org.opencypher.spark.prototype.api.graph

import org.opencypher.spark.prototype.api.ir.global.GlobalsRegistry

// (1) Import
// (2) Provide planner with graph
// (3) Plan current range of queries
// (4) Figure out physical plan
// (5) Execute and flesh out user facing api
trait GraphSpace {
  def base: CypherGraph
  def globals: GlobalsRegistry
//  def graphs: Set[Graph]
}
