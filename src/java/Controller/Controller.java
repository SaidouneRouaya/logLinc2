package Controller;
import Services.MDfromLogQueries.Declarations.Declarations;
import Services.MDfromLogQueries.Util.FileOperation;
import Services.MDfromLogQueries.Util.ModelUtil;
import Services.MDfromLogQueries.Util.TdbOperation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

import static Services.MDfromLogQueries.Declarations.Declarations.*;

@org.springframework.stereotype.Controller

public class Controller {

    private Map<String, Object> times = FileOperation.loadYamlFile(timesFilePathTest);
    private Map<String, Object> queriesNumbers = FileOperation.loadYamlFile(queriesNumberFilePathTest);
    Declarations declarations = new Declarations();

    @RequestMapping("/")
    public String redirect(){
        return "redirect:/index.j";
    }

    @RequestMapping("/index")
    public String pageAccueil(Model model) {

        String error = "";

        model.addAttribute("error", error);
        return "index2";
    }

    @RequestMapping("/beforeGraphs")
    public String beforeGraphs(Model model) {

        String error = "";

        model.addAttribute("error", error);
        return "subjectsBlocks";
    }

    @RequestMapping("/chooseScenario")
    public String chooseScnerio(Model model) {

        String error = "";

        model.addAttribute("error", error);
        return "chooseScenario";
    }


    @RequestMapping("/cleaning")
    public String Cleaning(Model model, @RequestParam String endpoint) {
        System.out.println(endpoint);
        if (!endpoint.isEmpty())
            Declarations.setEndpoint(endpoint);
        String error = "";
         times = FileOperation.loadYamlFile(timesFilePathTest);
        queriesNumbers = FileOperation.loadYamlFile(queriesNumberFilePathTest);

        System.out.println(times.get("Deduplication"));
        model.addAttribute("timesMap", times);

        model.addAttribute("queriesNumbersMap", queriesNumbers);

        model.addAttribute("error", error);
        return "cleaning";
    }

    @RequestMapping("/execution")
    public String executing(Model model) {

        String error = "";

        model.addAttribute("timesMap", times);

        model.addAttribute("queriesNumbersMap", queriesNumbers);

        model.addAttribute("error", error);
        return "execution";
    }

    @RequestMapping("/testTree")
    public String pageTree(Model model) {
        Set<JSONObject> models = new HashSet<>();
        JSONArray jsonArray = new JSONArray();

        HashMap<String, org.apache.jena.rdf.model.Model> modelHashMap = TdbOperation.unpersistNumberOfModelsMap(TdbOperation.dataSetAnnotated, 10);
        Iterator<String> kies = modelHashMap.keySet().iterator();
        while (kies.hasNext()) {
            String key = kies.next();
            if (modelHashMap.get(key).size() < 100)
                jsonArray.add(ModelUtil.modelToJSON(modelHashMap.get(key), key));
        }
        String erreur = "";
        System.out.println(jsonArray.toJSONString());
        model.addAttribute("models", jsonArray);
        model.addAttribute("erreur", erreur);
        return "MDGraph";
    }


    @RequestMapping("/subjectsBlocks")
    public String subjectsBlocks(Model model) {
        String error = "Error";

        JSONArray jsonArray = new JSONArray();
        JSONArray jsonArrayGlobal = new JSONArray();

        HashMap<String, org.apache.jena.rdf.model.Model> modelHashMap = TdbOperation.unpersistNumberOfModelsMap(TdbOperation.dataSetAnnotated, 20);

        Set<String> kies = modelHashMap.keySet();
        int i = 1;

        for (String key : kies) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("name", key);
            jsonObject.put("value", 10);

            //    jsonObject.put( "listeners", " [{ 'event' : 'clickGraphItem', 'method': function(event) {  window.alert('Clicked on ');}]");

            jsonArray.add(jsonObject);

            if (jsonArray.size() == 5) {

                JSONObject jsonChildren = new JSONObject();
                jsonChildren.put("children", jsonArray);
                jsonChildren.put("name", "subjects" + i);
                jsonArrayGlobal.add(jsonChildren);
                i++;
                jsonArray = new JSONArray();
            }

        }


        model.addAttribute("subjects", jsonArrayGlobal.toJSONString());
        model.addAttribute("error", error);
        return "subjectsBlocks";
    }

}
