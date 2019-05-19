package Services.MDPatternDetection.AnnotationClasses;


import Services.MDfromLogQueries.Util.*;
import com.google.common.base.Stopwatch;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MDGraphAnnotated {
    private static XSDMeasure_Types xsd = new XSDMeasure_Types();
    private static Datatype_Types datatype_types = new Datatype_Types();
    private static Constants2 constants2 = new Constants2();
    private static Property annotProperty = new PropertyImpl("http://loglinc.dz/annotated");

    public static HashMap<String, Model> constructMDGraphs(HashMap<String, Model> hashMapModels) {
        // HashMap<String , Model > results= new HashMap<>();

        Iterator it = hashMapModels.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {

            Map.Entry<String, Model> pair = (Map.Entry) it.next();
            System.out.println(" annotation du model n° " + i++);
            construtMDGraph(pair.getKey(), pair.getValue());
        }
        return hashMapModels;
    }


    public static void construtMDGraph(String modelSubject, Model model) {
        Resource subject = null;
        String propertyType;
        Statement statement;
        Property property;
        //Iterator<Resource> subjects = mdModel.listSubjects();
        ConstantsUtil constantsUtil = new ConstantsUtil();
        Set<RDFNode> visitedNodes = new HashSet<>();
        if (model.getResource(modelSubject) != null)
            subject = model.getResource(modelSubject);
        else
            subject = model.listSubjects().next();

        if (subject != null) {
            subject.addProperty(annotProperty, Annotations.FACT.toString());
            visitedNodes.add(subject);
            List<Statement> propertyIterator = subject.listProperties().toList();

            int nb_statement = 0;
            for (Statement stat : propertyIterator) {

                nb_statement++;
                System.out.println(" ---  statement nb : " + nb_statement);
                statement = stat;
                property = statement.getPredicate();
                try {
                    if (!property.equals(annotProperty)) {
                        propertyType = constantsUtil.getPropertyType(property);
                        //  System.out.println(" predicat :"+property+ "type dialha : "+propertyType);
                        switch (propertyType) {
                            case ("datatypeProperty"): {
                                if (XSDMeasure_Types.types.contains(statement.getObject().asResource())) {
                                    statement.getObject().asResource().addProperty(annotProperty, Annotations.MEASURE.toString());

                                } else {
                                    statement =  statement.changeObject(new ResourceImpl(statement.getObject().toString() + "FACT"));
                                    statement.getObject().asResource().addProperty(annotProperty, Annotations.FACTATTRIBUTE.toString());
                                }
                            }
                            break;
                            case ("objectProperty"): {
                                statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSION.toString());
                                addDimensionLevels(statement.getObject().asResource(), constantsUtil,visitedNodes);
                            }
                            break;
                            default: {
                                //   if (constantsUtil.askDatatypePropEndpoint(property, "https://dbpedia.org/sparql") || statement.getObject().asNode().getURI().matches("http://www.w3.org/2000/01/rdf-schema#Literal")) {
                                if (statement.getObject().equals(RDFS.Literal) ||
                                        statement.getObject().asResource().getNameSpace().matches(XSD.getURI()) ||
                                        Datatype_Types.types.contains(statement.getObject().asResource())) {
                                    if (XSDMeasure_Types.types.contains(statement.getObject().asResource()))
                                        statement.getObject().asResource().addProperty(annotProperty, Annotations.MEASURE.toString());
                                    else {
                                        statement =  statement.changeObject(new ResourceImpl(statement.getObject().toString() + "FACT"));
                                        statement.getObject().asResource().addProperty(annotProperty, Annotations.FACTATTRIBUTE.toString());
                                    }
                                } else {
                                    statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSION.toString());
                                    addDimensionLevels(statement.getObject().asResource(), constantsUtil,visitedNodes);
                                }
                            }
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        // return model;
    }

    public static void addDimensionLevels(Resource dimension, ConstantsUtil constantsUtil, Set<RDFNode> visitedNodes) {
        //Statement statement;
        Property property;
        String propertyType;
        List<Statement> propertyIterator = dimension.listProperties().toList();
        for (Statement statement : propertyIterator) {
            //statement = (Statement) propertyIterator.next();
            property = statement.getPredicate();

            try {

                if (!property.equals(annotProperty) && !visitedNodes.contains(statement.getObject())) {
                    propertyType = constantsUtil.getPropertyType(property);
                    switch (propertyType) {
                        case ("datatypeProperty"): {
                            statement =  statement.changeObject(new ResourceImpl(statement.getObject().toString() + "DIMENSION"));
                            statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSIONATTRIBUTE.toString());
                        }
                        break;
                        case ("objectProperty"): {

                            statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSIONLEVEL.toString());
                            statement.getObject().asResource().addProperty(new PropertyImpl(Annotations.PARENTLEVEL.toString()), dimension);
                            visitedNodes.add(dimension);
                            addDimensionLevels(statement.getObject().asResource(), constantsUtil, visitedNodes);
                        }
                        break;
                        default: {
                            if (statement.getObject().equals(RDFS.Literal) ||
                                    statement.getObject().asResource().getNameSpace().matches(XSD.getURI()) ||
                                    Datatype_Types.types.contains(statement.getObject().asResource())) {
                                statement =  statement.changeObject(new ResourceImpl(statement.getObject().toString() + "DIMENSION"));
                                statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSIONATTRIBUTE.toString());
                            } else {
                                statement.getObject().asResource().addProperty(annotProperty, Annotations.DIMENSIONLEVEL.toString());
                                statement.getObject().asResource().addProperty(new PropertyImpl(Annotations.PARENTLEVEL.toString()), dimension);
                                visitedNodes.add(dimension);
                                addDimensionLevels(statement.getObject().asResource(), constantsUtil, visitedNodes);
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch stopwatchunpersist = Stopwatch.createStarted();
        Stopwatch stopwatchannotation = Stopwatch.createStarted();


        HashMap<String, Model> modelsConsolidated = TdbOperation.unpersistModelsMap(TdbOperation.dataSetConsolidate);

        stopwatchunpersist.stop();
        System.out.println("time  unpersist : " + stopwatchunpersist);

        HashMap<String, Model> modelsAnnotated = constructMDGraphs(modelsConsolidated);
        stopwatchannotation.stop();
        System.out.println(" time annotation " + stopwatchannotation);

        TdbOperation.persistHashMap(modelsAnnotated, TdbOperation.dataSetAnnotated);

        stopwatch.stop();
        System.out.println("\n Time elapsed for the program is " + stopwatch.elapsed(SECONDS));

    }


}
