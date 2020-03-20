package embl.ebi.intact.helloWorld.internal.task;

import embl.ebi.intact.helloWorld.internal.model.Functions;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class HelloWorldTask extends AbstractTask {
    private final CyServiceRegistrar registrar;

    public HelloWorldTask(CyServiceRegistrar registrar) {
        this.registrar = registrar;

    }

    public static File createFile(String path) {
        ClassLoader cl = HelloWorldTask.class.getClassLoader();
        URL url = cl.getResource(path);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                System.err.println("Couldn't convert to URI : " + url);
            }
        } else {
            System.err.println("Couldn't find file: " + path);
        }
        return null;
    }


    @Override
    public void run(TaskMonitor taskMonitor) {
        List<Functions> toExecute = new ArrayList<>();
        int numberOfExecutions = 20;
        for (Functions fun : Functions.values()) {
            toExecute.addAll(Collections.nCopies(numberOfExecutions, fun));
        }
        Collections.shuffle(toExecute);

        Map<Functions, List<Duration>> durations = new HashMap<>();
        for (Functions fun : toExecute) {
            System.out.println(fun);
            Duration duration = fun.execute();
            if (durations.containsKey(fun)) {
                durations.get(fun).add(duration);
            } else {
                durations.put(fun, new ArrayList<Duration>() {{
                    add(duration);
                }});
            }
        }

        System.out.println("durationInMs, format, output, dataset");
        for (Map.Entry<Functions, List<Duration>> entry : durations.entrySet()) {
            for (Duration duration : entry.getValue()) {
                String csvDetails = "";
                switch (entry.getKey()) {
                    case JSON_FILE:
                        csvDetails = ", json, file";
                        break;
                    case JSON_STREAM:
                        csvDetails = ", json, stream";
                        break;
                    case CSV_FILE:
                        csvDetails = ", csv, file";
                        break;
                    case CSV_STREAM:
                        csvDetails = ", csv, stream";
                        break;
                }
                System.out.println(duration.toMillis() + csvDetails + ", whole");
            }
        }

    }

    public static String query = "CALL apoc.export.{format}.query(\n" +
            "\"MATCH (interactorA:GraphInteractor)<-[:interactors]-(interaction:GraphBinaryInteractionEvidence)-[:interactors]->(interactorB:GraphInteractor)\n" +
            "WHERE  ID(interactorA)<ID(interactorB) AND EXISTS(interactorA.uniprotName) AND EXISTS(interactorB.uniprotName)\n" +
            "OPTIONAL MATCH (interaction)-[identifiersR:identifiers]-(identifiersN:GraphXref)-[sourceR:database]-(sourceN:GraphCvTerm) WHERE sourceN.shortName IN ['reactome','signor','intact']\n" +
            "OPTIONAL MATCH (interaction)-[interactiontypeR:interactionType]-(interactiontypeN:GraphCvTerm)\n" +
            "OPTIONAL MATCH (interaction)-[experimentR:experiment]-(experimentN:GraphExperiment)-[interactionDetectionMethodR:interactionDetectionMethod]-(interactionDetectionMethodN:GraphCvTerm)\n" +
            "OPTIONAL MATCH (experimentN)-[hostOrganismR:hostOrganism]-(hostOrganismN:GraphOrganism)\n" +
            "OPTIONAL MATCH (experimentN)-[participantIdentificationMethodR:participantIdentificationMethod]-(participantIdentificationMethodN:GraphCvTerm)\n" +
            "OPTIONAL MATCH (experimentN)-[publicationR:PUB_EXP]-(publicationN:GraphPublication)-[pubmedIdXrefR:pubmedId]-(pubmedIdXrefN:GraphXref)\n" +
            "OPTIONAL MATCH (interaction)-[clusteredInteractionR:interactions]-(clusteredInteractionN:GraphClusteredInteraction)\n" +
            "OPTIONAL MATCH (interaction)-[complexExpansionR:complexExpansion]-(complexExpansionN:GraphCvTerm)\n" +
            "RETURN\n" +
            "       distinct\n" +
            "       interactorA.uniprotName as interactorA_uniprot_name,\n" +
            "       interactorB.uniprotName as interactorB_uniprot_name,\n" +
            "       interactiontypeN.shortName as interaction_type_short_name,\n" +
            "       interactiontypeN.mIIdentifier as interaction_type_mi_identifier,\n" +
            "       interactionDetectionMethodN.shortName as interaction_detection_method_short_name,\n" +
            "       interactionDetectionMethodN.mIIdentifier as interaction_detection_method_mi_identifier,\n" +
            "       hostOrganismN.scientificName as host_organism_scientific_name,\n" +
            "       hostOrganismN.taxId as host_organism_tax_id,\n" +
            "       participantIdentificationMethodN.shortName as participant_detection_method_short_name,\n" +
            "       participantIdentificationMethodN.mIIdentifier as participant_detection_method_mi_identifier,\n" +
            "       clusteredInteractionN.miscore as mi_score,\n" +
            "       pubmedIdXrefN.identifier as pubmed_id,\n" +
            "       COLLECT(identifiersN.identifier) as interaction_identifier,\n" +
            "       CASE WHEN complexExpansionN.shortName IS NULL THEN 'Not Needed' ELSE complexExpansionN.shortName END as expansion_method_short_name,\n" +
            "       CASE WHEN complexExpansionN.mIIdentifier IS NULL THEN 'Not Needed' ELSE complexExpansionN.mIIdentifier END as expansion_method_mi_identifier,\n" +
            "       COLLECT (sourceN.shortName) as source_databases\n" +
            "       ORDER BY interactorA_uniprot_name\n" +
            "\",\n" +
            "{file},{stream:true}\n" +
            ")\n" +
            "YIELD file, nodes, relationships, properties, data\n" +
            "RETURN file, nodes, relationships, properties, data";

    public static Duration queryServer(String exportMethod, boolean withStream) {
        Instant begin = Instant.now();

        try {
            Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "1234"));
            String currentQuery = query.replace("{format}", exportMethod);
            if (withStream)
                currentQuery = currentQuery.replace("{file}", "null");
            else
                currentQuery = currentQuery.replace("{file}", "\"test." + exportMethod + "\"").replace("{stream:true}", "{}");
            StatementResult result = driver.session().run(currentQuery);
            System.out.println(result.consume());
            driver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Result records displayed :");
        return displayTimeElapsed(begin, Instant.now());

    }

    public static Duration displayTimeElapsed(Instant before, Instant after) {
        Duration duration = Duration.between(before, after);
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        System.out.println(seconds < 0 ? "-" + positive : positive);
        return duration;
    }


    public static Duration queryLocalNeo4jServerWithJsonStreamed() {
        return queryServer("json", true);
    }

    public static Duration queryLocalNeo4jServerWithCSVStreamed() {
        return queryServer("csv", true);
    }

    public static Duration queryLocalNeo4jServerWithCSVFile() {
        return queryServer("csv", false);
    }

    public static Duration queryLocalNeo4jServerWithJsonFile() {
        return queryServer("json", false);
    }

}
