package Services.Scenarios

import java.util

import Services.MDPatternDetection.AnnotationClasses.MDGraphAnnotated
import Services.MDPatternDetection.ConsolidationClasses.ConsolidationParallel._
import Services.MDPatternDetection.EnrichmentClasses.EnrichParallel.enrichMDSchema
import Services.MDfromLogQueries.Declarations.Declarations
import Services.MDfromLogQueries.Util.{FileOperation, ModelUtil, TdbOperation}
import Services.Statistics.Statistics1
import org.apache.jena.rdf.model.Model

object Scenario_Enrichment extends App{



  Declarations.setEndpoint("dbPedia")
  /** 1. Unpersisting of annotated models **/
  var t_total: Long = System.currentTimeMillis()
  var endpoint = "https://dbpedia.org/sparql"
  val modelsAnnotated: util.HashMap[String, Model] = TdbOperation.unpersistModelsMap(Declarations.paths.get("dataSetAnnotated")+"selection")


  /** 2. Annotate non alleviated models **/
  var t_annotation: Long = System.currentTimeMillis()
  val modelsNonAlleviated: util.HashMap[String, Model] = TdbOperation.unpersistModelsMap(TdbOperation.dataSetNonAlleviated)
  val modelsNonAlleviatedAnnotated : util.HashMap[String, Model] = MDGraphAnnotated.constructMDGraphs(modelsNonAlleviated)
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "modelsNonAlleviatedAnnotation", (System.currentTimeMillis() - t_annotation).toInt)
  modelsAnnotated.putAll(modelsNonAlleviatedAnnotated)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"),"Average_size_before_enrichment",ModelUtil.averageSize(modelsAnnotated).toInt)


  /** 3. Enrichment of annotated models **/
  var t_enrichment: Long = System.currentTimeMillis()
  enrichMDSchema(modelsAnnotated,endpoint)
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Enrichment", (System.currentTimeMillis() - t_enrichment).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"),"Average_size_after_enrichment",ModelUtil.averageSize(modelsAnnotated).toInt)


  /** 4. Annotation of new models **/
  var t_annotation2 : Long = System.currentTimeMillis()
  val enrichedModelsAnnotated : util.HashMap[String, Model] = MDGraphAnnotated.constructMDGraphs(modelsAnnotated)
  writeInTdb(convertToScalaMap(enrichedModelsAnnotated), Declarations.paths.get("dataSetEnrichedAnnotated"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "enrichedModelsAnnotation", (System.currentTimeMillis() - t_annotation2).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"),"Average_size_after_enrichment_annotation",ModelUtil.averageSize(modelsAnnotated).toInt)


  /** 5. Statistics  **/
  var t_statistics: Long = System.currentTimeMillis()
  var statistics : Statistics1 = new Statistics1
  val stat = statistics.stat2(TdbOperation.unpersistModelsMap(Declarations.paths.get("dataSetEnrichedAnnotated")))
  Statistics1.writeAllStatsInYAML(stat,Declarations.paths.get("enrichedStatisticsFileYAML"),Declarations.paths.get("enrichedStatisticsByTypeFile"))
  //statisticsBySubjectList(subjects)
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "StatisticsEnriched", (System.currentTimeMillis() - t_statistics).toInt)

}
