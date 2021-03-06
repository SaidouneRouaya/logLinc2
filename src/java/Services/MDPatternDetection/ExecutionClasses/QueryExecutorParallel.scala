package Services.MDPatternDetection.ExecutionClasses

import java.io.{File, FileOutputStream, PrintWriter}

import Services.MDfromLogQueries.Declarations.Declarations
import Services.MDfromLogQueries.Util.TdbOperation
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.tdb.TDB
import QueryExecutorParallelFuture.queriesLogNumber

import scala.collection.parallel.ParSeq
import scala.io.Source


object QueryExecutorParallel   {

  var nb_model = 0

  def executeQueriesInFile(filePath: String, endPoint: String) = {

    val queryExecutor = new QueryExecutor

    val constructQueriesList = Source.fromFile(filePath).getLines

    constructQueriesList.grouped(100000).foreach {
      groupOfLines => {
        var nb_req = 0
        //var nonValidQueries : ParSeq[Query] = ParSeq()
        val treatedGroupOfLines = groupOfLines.par.map {
          line => {
            nb_req += 1
            println("Requete\t" + nb_req)
            var model = ModelFactory.createDefaultModel()
            try {
              val query = QueryFactory.create(line)
              model = queryExecutor.executeQueryConstruct(query, endPoint)

              if (model != null) {
                Right(Some(model))
              } else Right(None)

            }
            catch {
              case exp: Exception => {
                println("une erreur\n\n\n\n\n\n\n\n\n")
                //nonValidQueries.+:(constructedQuery)
                Left(line)
              }
            }
          }
        }

        println("--------------------- un group finished ---------------------------------- ")
        val (correct, errors) = treatedGroupOfLines.partition(_.isRight)
        writeInTdb(correct.collect { case Right(Some(x)) => x })

        writeInLogFile(Declarations.paths.get("executionLogFile"), errors.collect { case Left(line) => line })

      }

    }
  }

  def writeInLogFile(destinationFilePath: String, queries: ParSeq[String]) = {
    val writer = new PrintWriter(new FileOutputStream(new File(destinationFilePath), true))

    queries.foreach(query => writer.write(query.replaceAll("[\n\r]", "\t") + "\n"))

    writer.close()
  }

  def writeInTdb(models: ParSeq[Model]) = {
    val tdb = new TdbOperation()
    var nb_model = 0

    models.foreach(m => {
      nb_model += 1
      TdbOperation.originalDataSet.addNamedModel("model_" + nb_model, m)
      //   TdbOperation.originalDataSetTest.addNamedModel("model_" + nb_model, m)
    })


  }

  def writeInLogFile(destinationFilePath: String, queries: Vector[String]) = {

    val writer = new PrintWriter(new FileOutputStream(new File(destinationFilePath), true))

    queries.foreach(query => {
      queriesLogNumber+=1
      writer.write(query.replaceAll("[\n\r]", "\t") + "\n")
    })

    writer.close()
  }



  def writeInTdb(models: Vector[Model]) = {

    models.foreach(m => {

      if (m != null) {
        nb_model += 1
        println("write " + nb_model)
        TdbOperation.originalDataSet.addNamedModel("model_" + nb_model, m)
      }
    })
    TDB.sync(TdbOperation.originalDataSet)

  }

}
