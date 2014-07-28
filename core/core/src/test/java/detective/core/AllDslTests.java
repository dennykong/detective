package detective.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import detective.core.runner.DataSharedInStoryLevelTest;
import detective.core.runner.DataTableRunnerTest;
import detective.core.runner.ParallelRunnerTest;
import detective.core.runner.SimpleStoryRunnerTest;
import detective.core.runner.TableParserTest;
import detective.core.testdsl.DslBuilderTest;

@RunWith(Suite.class)
@SuiteClasses(
  {
    DslBuilderTest.class,
    SimpleStoryRunnerTest.class,
    TableParserTest.class,
    DataTableRunnerTest.class,
    DataSharedInStoryLevelTest.class,
    ParallelRunnerTest.class
  })
public class AllDslTests {

}