/*
===============================================================
ResumableBoundaryWalker.java
Use the aril language and the support specified in the
configuration file IssProtocolConfig.txt

The business logic is defined in RobotControllerArilBoundary
that is 'message-driven'
===============================================================
*/
package it.unibo.wenv;
import it.unibo.annotations.ArilRobotSpec;
import it.unibo.consolegui.ConsoleGui;
import it.unibo.interaction.IssOperations;
import it.unibo.supports.IssCommSupport;
import it.unibo.supports.RobotApplicationStarter;

@ArilRobotSpec
public class ResumableBoundaryWalker {
    private RobotInputController controller;

    //Constructor
    public ResumableBoundaryWalker(IssOperations rs){
        IssCommSupport rsComm = (IssCommSupport)rs;
        controller = new RobotInputController(rsComm, true, true );
        rsComm.registerObserver( controller );
        System.out.println("ResumableBoundaryWalker | CREATED with rsComm=" + rsComm);

        /*prova fatta in classe (quando viene invocato l'update observer viene updato anche questo)
        gli observer sono molto interessanti perchè permettono di aggiungere componenti senza modificare la struttura
        AnotherObserver obs = new AnotherObserver();
        rsComm.registerObserver(obs);
        */

        new ConsoleGui(controller);
        controller.doBoundary();
    }


    public static void main(String args[]){
        try {
            System.out.println("ResumableBoundaryWalker | main start n_Threads=" + Thread.activeCount());
            //crea istanza classe
            RobotApplicationStarter.createInstance(ResumableBoundaryWalker.class);
            System.out.println("ResumableBoundaryWalker  | appl n_Threads=" + Thread.activeCount());

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
}
