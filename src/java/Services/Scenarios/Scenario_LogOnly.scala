package Services.Scenarios

import Services.MDPatternDetection.Alleviation.MDGraphsAlleviation
import Services.MDPatternDetection.AnnotationClasses.MDGraphAnnotated
import Services.MDPatternDetection.ConsolidationClasses.ConsolidationParallel
import Services.MDPatternDetection.ConsolidationClasses.ConsolidationParallel._
import Services.MDPatternDetection.ExecutionClasses.QueryExecutorParallelFuture
import Services.MDPatternDetection.ExecutionClasses.QueryExecutorParallelFuture.executeQueriesInFile
import Services.MDPatternDetection.GraphConstructionClasses.Queries2GraphesParallel
import Services.MDPatternDetection.GraphConstructionClasses.Queries2GraphesParallel.TransformQueriesInFile
import Services.MDfromLogQueries.Declarations.Declarations
import Services.MDfromLogQueries.LogCleaning.{LogCleaningOneFile, QueriesDeduplicator}
import Services.MDfromLogQueries.LogCleaning.QueriesDeduplicator.DeduplicateQueriesInFile
import Services.MDfromLogQueries.SPARQLSyntacticalValidation.SyntacticValidationParallel
import Services.MDfromLogQueries.SPARQLSyntacticalValidation.SyntacticValidationParallel.valideQueriesInFile
import Services.MDfromLogQueries.Util.{FileOperation, TdbOperation}
import Services.Statistics.Statistics1
import org.apache.jena.rdf.model.Model
import java.util




import Services.MDPatternDetection.ConsolidationClasses.ConsolidationParallel
import Services.MDPatternDetection.ExecutionClasses.QueryExecutorParallelFuture
import Services.MDPatternDetection.GraphConstructionClasses.Queries2GraphesParallel
import Services.MDPatternDetection.GraphConstructionClasses.Queries2GraphesParallel.TransformQueriesInFile
import Services.MDfromLogQueries.Declarations.Declarations
import Services.MDfromLogQueries.LogCleaning.{LogCleaningOneFile, QueriesDeduplicator}
import Services.MDfromLogQueries.SPARQLSyntacticalValidation.SyntacticValidationParallel
import Services.MDfromLogQueries.Util.FileOperation


object Scenario_LogOnly extends App{

  val endpoint="dbPedia"
  val endpointUrl="http://linkedgeodata.org/sparql/"
  Declarations.setEndpoint(endpoint)

  /** 1. Nettoyage du log **/
  var t_cleaning: Long = System.currentTimeMillis()
  LogCleaningOneFile.writeFiles(Declarations.paths.get("directoryPath"), Declarations.paths.get("cleanedQueriesFile"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Log_Cleaning", (System.currentTimeMillis() - t_cleaning).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Log_Cleaning_nbLines", LogCleaningOneFile.nbLines)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Log_Cleaning_nbQueries", LogCleaningOneFile.queriesNumber)

  /** 2. Deduplication **/
  var t_dedup: Long = System.currentTimeMillis()
  DeduplicateQueriesInFile(Declarations.paths.get("cleanedQueriesFile"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Deduplication", (System.currentTimeMillis() - t_dedup).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Deduplication", QueriesDeduplicator.queriesNumber)

  /** 3. Validaion syntaxique **/
  var t_syntacticValidation: Long = System.currentTimeMillis()
  valideQueriesInFile(Declarations.paths.get("deduplicatedQueriesFile"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Syntactical_Validation", (System.currentTimeMillis() - t_syntacticValidation).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Syntactical_Validation", SyntacticValidationParallel.queriesNumber )




  /** 4. Construct MD graphs **/
  var t_connstructMDgraphs: Long = System.currentTimeMillis()
  TransformQueriesInFile(Declarations.paths.get("syntaxValidFile2"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "ConstructMSGraphs", (System.currentTimeMillis() -  t_connstructMDgraphs).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "ConstructMSGraphs_nbQueriesConstructed", Queries2GraphesParallel.queriesNumber)


  /** 5. Execution **/
  var t_execution: Long = System.currentTimeMillis()
  executeQueriesInFile(Declarations.paths.get("constructQueriesFile2"), endpointUrl)
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Execution", (System.currentTimeMillis() - t_execution).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Execution_nbQueriesExecutedWithNullModels", QueryExecutorParallelFuture.queriesNumber)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Execution_nbQueriesExecutedWithModels", QueryExecutorParallelFuture.queriesNumberwithModels)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Execution_nbQueriesNonExecuted", QueryExecutorParallelFuture.queriesLogNumber)

  /** 6. Alleviation 1 (Useless properties removement) **/
  println("***********************Alleviation 1******************")
  var t_alleviation1: Long = System.currentTimeMillis()
  val modelsAlleviated_UselessProp: util.HashMap[String, Model]=MDGraphsAlleviation.removeUselessProperties( TdbOperation.unpersistModelsMap(TdbOperation.originalDataSet))
  writeInTdb(convertToScalaMap(modelsAlleviated_UselessProp), Declarations.paths.get("dataSetAlleviatedUselessProperties"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Alleviation_UselessProperties", (System.currentTimeMillis() - t_alleviation1).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Alleviation_nbStatementsRemoved", MDGraphsAlleviation.numberStatementRemoved)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Alleviation_nbModelAlleviated", MDGraphsAlleviation.numberModelAlleviated)


  /** 7. Consolidation **/
  println("***********************Consolidation******************")
  var t_consolidation: Long = System.currentTimeMillis()
  //writeInTdb(consolidate(), TdbOperation.dataSetConsolidate)
  //writeInTdb(consolidate(), Declarations.paths.get("dataSetConsolidated"))
  //toStringModelsHashmap2(unpersistModelsMap(Declarations.paths.get("dataSetAlleviatedUselessProperties")),Declarations.paths.get("_toString"))
  consolidateParallel(Declarations.paths.get("dataSetConsolidated"))
  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Consolidation", (System.currentTimeMillis() - t_consolidation).toInt)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Consolidation_nbModelsB4Consolidation", ConsolidationParallel.originalModelsNumber)
  FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Consolidation_nbModels", ConsolidationParallel.modelsNumber) // after consolidation


  /** 8. Alleviation 2  (Small graph removement) **/
  println("***********************Alleviation 2******************")
  var t_alleviation: Long = System.currentTimeMillis()
  val modelsAlleviated: util.HashMap[String, Model]=MDGraphsAlleviation.MDGraphsAlleviate( TdbOperation.unpersistModelsMap(Declarations.paths.get("dataSetConsolidated")))
 // writeInTdb(convertToScalaMap(modelsAlleviated), TdbOperation.dataSetAlleviated)
  writeInTdb(convertToScalaMap(modelsAlleviated), Declarations.paths.get("dataSetAlleviated"))
 //FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Alleviation", (System.currentTimeMillis() - t_alleviation).toInt)
 // FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Alleviation_nbModelsRemoved", MDGraphsAlleviation.numberModelsRemoved)
  //FileOperation.writeInYAMLFile(Declarations.paths.get("queriesNumberFilePath"), "Alleviation_nbModels", MDGraphsAlleviation.numberModelsAlleviated)

  /** 9. Annotation **/
  println("***********************Annotation******************")
  var t_annotation: Long = System.currentTimeMillis()
  val modelsAlleviate: util.HashMap[String, Model] = TdbOperation.unpersistModelsMap(Declarations.paths.get("dataSetAlleviated"))
  val modelsAnnotated : util.HashMap[String, Model] = MDGraphAnnotated.constructMDGraphs(modelsAlleviate)
  //writeInTdb(convertToScalaMap(modelsAnnotated), TdbOperation.dataSetAnnotated)
  writeInTdb(convertToScalaMap(modelsAnnotated), Declarations.paths.get("dataSetAnnotated"))
   // FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Annotation", (System.currentTimeMillis() - t_annotation).toInt)


  /** 10. Stistique **/
  println("***********************Statistiques******************")
  var t_statistics: Long = System.currentTimeMillis()
  var statistics : Statistics1 = new Statistics1
  val stat = statistics.stat2(TdbOperation.unpersistModelsMap(Declarations.paths.get("dataSetAnnotated")))

  Statistics1.writeAllStatsInYAML(stat, Declarations.paths.get("statisticsFileYAML"), Declarations.paths.get("statisticsByTypeFile"))

  //statisticsBySubjectList(subjects)

  FileOperation.writeInYAMLFile(Declarations.paths.get("timesFilePath"), "Statistics", (System.currentTimeMillis() - t_statistics).toInt)

  //TODO ecrire dans un fichier les stat concernant nombre de req ..Etc

}
