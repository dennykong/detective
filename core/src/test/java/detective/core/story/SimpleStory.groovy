package detective.core.story

import static detective.core.Detective.*;


story() "Simple Story with Echo Task" {
  inOrderTo "demostrate simple story"
  
  scenario "Echo parameters back" {
    given "a parameter" {
      parameterA = "This is the value"
    }
    
    when "run echo task"{
      runtask echoTask();
    }
    
    then "parameters will echo back"{
      echotask.parameterA << "This is the value"
    }
  }
}


story() "Demostrate different way" {
  scenario {
    given "a parameter" {
      parameterA = "This is the value"
    }
    
    "or without given, direct write description here" {
      parameterB = "This is parameter b"
    }
    
    when "run echo task"{
      runtask echoTask();
    }
    
    then "parameters will echo back"{
      echotask.parameterA << "This is the value"
      echotask.parameterB << "This is parameter b"
    }
  }
}