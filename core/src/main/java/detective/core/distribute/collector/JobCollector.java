package detective.core.distribute.collector;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import detective.common.ClassUtils;
import detective.core.Detective;
import detective.core.Scenario;
import detective.core.Story;
import detective.core.distribute.Job;
import detective.core.filter.FilterChainFactory;
import detective.core.filter.RunnerFilter;
import detective.core.filter.RunnerFilterChain;
import detective.core.runner.DslBuilderAndRun;
import detective.core.runner.RunnerInterceptor;
import detective.core.runner.SimpleStoryRunner;

/**
 * Please be very careful, this class will change DslBuilderAndRun behavior to run storys
 * in spark not the normal process 
 * 
 * @author James Luo
 *
 */
public class JobCollector {
  
  private static Logger logger = LoggerFactory.getLogger(JobCollector.class);
  private FilterChainFactory chainFactory;
  
  private JobCollector(){
    try {
      chainFactory = FilterChainFactory.ConfigReader.instanceFromConfigFile("runner.spark_distribute.filter_chain_factory");
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
    DslBuilderAndRun.setFilterChainCurrentThread((RunnerFilterChain<Story>) chainFactory.getChain());
  }
  
  private List<Job> getJobs(){
   for (RunnerFilter<?> filter : chainFactory.getChain()){
     if (filter instanceof JobCollectorFilter){
       return ((JobCollectorFilter)filter).getJobs();
     }
   } 
   
   throw new RuntimeException("No detective.core.distribute.JobCollectorFilter found in filter chain : " + chainFactory.getClass().getName());
  }
  
  
  private void shutdown(){
    DslBuilderAndRun.setFilterChainCurrentThread(null);
  }
  
  public static List<Job> collectAll(String packageOrClassName){
    List<Job> jobs = new ArrayList<Job>();
    
    try {
      List<Class<?>> classes = collectClass(packageOrClassName);
      if (classes == null || classes.size() == 0)
        classes = ClassUtils.getClassesForPackage(packageOrClassName);
      
      for (Class<?> clazz : classes){
        //if (clazz.isAssignableFrom(Script.class)){ //not work for some reason
        if (clazz.getSuperclass().getName().equals("groovy.lang.Script")){
          jobs.addAll(collectJobs(clazz));
        }else{
          //logger.warn(clazz.getName() + " is not a groovy script or story, ignored.");
          //Don't log, too much info
        }
      }
    } catch (Exception e) {
      Detective.error(e.getMessage(), e);
    }
    
    return jobs;
  }
  
  private static List<Class<?>> collectClass(String className){
    try {
      Class<?> clazz = Class.forName(className);
      List<Class<?>> result = new ArrayList<Class<?>>();
      result.add(clazz);
      return result;
    } catch (ClassNotFoundException e) {
      return null;
    }
    
  }

  private static List<Job> collectJobs(Class<?> clazz) throws InstantiationException,
      IllegalAccessException {
    JobCollector dis = new JobCollector();
    try{
      Script script = (Script)clazz.newInstance();
      script.run();
      
      for (Job job : dis.getJobs())
        job.setStoryClassName(clazz.getName());
    }finally{
      dis.shutdown();
    }
    return dis.getJobs();
  }

}