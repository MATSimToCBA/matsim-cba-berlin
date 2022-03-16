package org.matsim.run.utils;

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.run.RunBerlinScenarioWithCba;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResumeSimulationFromLastPlans {

    private static final Logger log = Logger.getLogger(RunBerlinScenarioWithCba.class);

    public static void resumeSimulationFromLastPlans(Config config) {
        String outputDirectory = config.controler().getOutputDirectory();
        int writePlansInterval = config.controler().getWritePlansInterval();
        int firstIteration = -1;

        OutputDirectoryHierarchy outputDirectoryHierarchy = new OutputDirectoryHierarchy(config.controler().getOutputDirectory(), config.controler().getRunId(), OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles, false, config.controler().getCompressionType());


        Path plansPath = null;
        int lastIteration = config.controler().getLastIteration();
        if(!Files.exists(Paths.get(outputDirectory))) {
            log.warn("The output directory does not exist, will proceed with the simulation normally");
            return;
        }
        for(int i=0; i<=lastIteration; i+=writePlansInterval) {
            Path currentPlansPath = Paths.get(outputDirectoryHierarchy.getIterationFilename(i, Controler.DefaultFiles.population));
            if(Files.exists(currentPlansPath)) {
                plansPath = currentPlansPath;
                firstIteration = i;
            }
            else {
                break;
            }
        }
        if(firstIteration >= 0) {
            config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
            config.controler().setFirstIteration(firstIteration);
            plansPath = plansPath.toAbsolutePath();
            config.plans().setInputFile(plansPath.toString());
            log.info("Will resume the simulation from iteration " + firstIteration + " using the input plans file " + plansPath.toString());
        }
        else {
            log.warn("No output plan file was found in an iteration folder, will proceed with the simulation as usual");
        }
    }

}
